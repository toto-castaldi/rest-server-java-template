package com.github.totoCastaldi.restServer;

import com.github.totoCastaldi.restServer.request.BasicAuthorizationRequest;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by github on 08/11/15.
 */
public class ApiHeaderUtils {

    private static final String PREFIX_BASIC = "Basic ";
    private static final String COMMA = ",";
    private final BasicAuthorizationRequest.Factory basicAuthorizationFactory;

    @Inject
    public ApiHeaderUtils(
            BasicAuthorizationRequest.Factory basicAuthorizationFactory
    ) {
        this.basicAuthorizationFactory = basicAuthorizationFactory;
    }

    public Iterable<AuthorizationRequestInfo> parseAuthorization(String authSeparated, AuthenticationType... types) {
        return parseAuthorization(StringUtils.isNotBlank(authSeparated) ? Arrays.asList(StringUtils.splitByWholeSeparator(authSeparated, COMMA)) : Collections.<String>emptyList(), types);
    }

    public Iterable<AuthorizationRequestInfo> parseAuthorization(List<String> auths, AuthenticationType... types) {
        List<AuthenticationType> authenticationTypes = types.length > 0 ? Arrays.asList(types) : Arrays.asList(AuthenticationType.values());
        List<AuthorizationRequestInfo> result = Lists.newArrayList();
        if (auths != null) {
            for (String a : auths) {
                String auth = StringUtils.trimToEmpty(a);
                if (StringUtils.startsWith(auth, PREFIX_BASIC) && Iterables.contains(authenticationTypes, AuthenticationType.BASIC)) {
                    result.add(new AuthorizationRequestInfo(basicAuthorizationFactory.create(StringUtils.trimToEmpty(StringUtils.substringAfter(auth, PREFIX_BASIC))), AuthenticationType.BASIC));
                }
            }
        }
        return result;
    }

    public Iterable<AuthenticationType> parseAuthorizationRequestes(String authSeparated, AuthenticationType... types) {
        List<String> auths = StringUtils.isNotBlank(authSeparated) ? Arrays.asList(StringUtils.splitByWholeSeparator(authSeparated, COMMA)) : Collections.<String>emptyList();
        List<AuthenticationType> authenticationTypes = types.length > 0 ? Arrays.asList(types) : Arrays.asList(AuthenticationType.values());


        List<AuthenticationType> result = Lists.newArrayList();
        if (auths != null) {
            for (String a : auths) {
                String auth = StringUtils.trimToEmpty(a);
                if (StringUtils.startsWith(auth, PREFIX_BASIC) && Iterables.contains(authenticationTypes, AuthenticationType.BASIC)) {
                    result.add(AuthenticationType.BASIC);
                }
            }
        }
        return result;
    }

}