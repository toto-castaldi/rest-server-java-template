package com.github.totoCastaldi.restServer;

import com.github.totoCastaldi.commons.GuiceInjector;
import com.github.totoCastaldi.commons.MemoryShutdownableRepository;
import com.github.totoCastaldi.commons.ShutdownableRepository;
import com.github.totoCastaldi.restServer.authorization.BasicAuthorization;
import com.github.totoCastaldi.restServer.authorization.UserProfile;
import com.github.totoCastaldi.restServer.request.BasicAuthorizationRequest;
import com.github.totoCastaldi.restServer.response.ApiErrorMessage;
import com.github.totoCastaldi.restServer.response.ApiResponse;
import com.github.totoCastaldi.restServer.response.ErrorResponseEntry;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.util.Providers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.*;
import org.apache.commons.lang.BooleanUtils;

import java.util.List;

/**
 * Created by github on 05/12/14.
 */
@Slf4j
public abstract class ApiServletContextListener extends GuiceServletContextListener {

    public static Injector injector;

    public ApiServletContextListener(
    ){
    }

    @Override
    protected Injector getInjector() {
        log.info("Getting injector");

        final RestServerConf appModule = getAppConf();

        List<Module> modules = Lists.newArrayList();

        modules.add(

                new AbstractModule() {
                    @Override
                    protected void configure() {
                        final FactoryModuleBuilder factoryModuleBuilder = new FactoryModuleBuilder();

                        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
                        compositeConfiguration.addConfiguration(new SystemConfiguration());
                        compositeConfiguration.addConfiguration(new EnvironmentConfiguration());
                        try {
                            compositeConfiguration.addConfiguration(new PropertiesConfiguration("default.properties"));
                        } catch (ConfigurationException e) {
                            log.warn("",e);
                        }

                        Iterable<RestServerConf.AppConfKey> confKeys = appModule.getConfKeys();

                        for (RestServerConf.AppConfKey confKey : confKeys) {
                            if (confKey.getClazz() == RestServerConf.TYPE.STRING) bind(String.class).annotatedWith(Names.named(confKey.getName())).toInstance(compositeConfiguration.getString(confKey.getName()));
                            if (confKey.getClazz() == RestServerConf.TYPE.INTEGER) bind(Integer.class).annotatedWith(Names.named(confKey.getName())).toInstance(Integer.valueOf(compositeConfiguration.getString(confKey.getName())));
                            if (confKey.getClazz() == RestServerConf.TYPE.BOOLEAN) bind(Boolean.class).annotatedWith(Names.named(confKey.getName())).toInstance(BooleanUtils.toBoolean(compositeConfiguration.getString(confKey.getName())));
                        }

                        bind(ApiResponse.class);
                        bind(ApiHeaderUtils.class);
                        bind(ApiScheduler.class);
                        bind(TimeProvider.class);
                        bind(ShutdownableRepository.class).to(MemoryShutdownableRepository.class);
                        bind(ApiErrorMessage.class).toInstance(appModule.getApiErrorMessage());

                        if (appModule.getBasicAuthorizationSupport() != null) {
                            bind(BasicAuthorization.class).to(appModule.getBasicAuthorizationSupport());
                        } else {
                            bind(BasicAuthorization.class).toProvider(Providers.of((BasicAuthorization)null));
                        }

                        if (appModule.getUserTypeSupport() != null) {
                            bind(UserProfile.class).to(appModule.getUserTypeSupport());
                        } else {
                            bind(UserProfile.class).toProvider(Providers.of((UserProfile)null));
                        }

                        install(factoryModuleBuilder.build(BasicAuthorizationRequest.Factory.class));
                        install(factoryModuleBuilder.build(ErrorResponseEntry.Factory.class));
                        install(factoryModuleBuilder.build(ApiCurrentExecution.Factory.class));
                    }
                }
        );

        modules.addAll(appModule.getModules());

        injector = Guice.createInjector(modules);

        GuiceInjector.setIstance(injector);

        JerseyResources.addPackages(appModule.getPackages());
        JerseyResources.addContainerRequestFilters(appModule.getFilters());

        return injector;

    }

    protected abstract RestServerConf getAppConf();

}