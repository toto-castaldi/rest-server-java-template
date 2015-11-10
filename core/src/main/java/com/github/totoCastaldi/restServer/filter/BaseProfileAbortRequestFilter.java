package com.github.totoCastaldi.restServer.filter;

import com.github.totoCastaldi.restServer.ApiCurrentExecution;
import com.github.totoCastaldi.restServer.ApiHeaderUtils;
import com.github.totoCastaldi.restServer.UserType;
import com.github.totoCastaldi.restServer.response.ApiResponse;
import com.github.totoCastaldi.restServer.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

/**
 * Created by github on 08/11/15.
 */
@Slf4j
public abstract class BaseProfileAbortRequestFilter implements ContainerRequestFilter {

    private final ApiHeaderUtils apiHeaderUtils;
    private final ApiResponse apiResponse;
    private final HttpServletRequest httpRequest;
    private final UserType role;
    private final ErrorResponse.DetailsCode errorCode;

    public BaseProfileAbortRequestFilter(
            ApiHeaderUtils apiHeaderUtils,
            ApiResponse apiResponse,
            HttpServletRequest httpRequest,
            UserType role,
            ErrorResponse.DetailsCode errorCode
    ) {
        this.apiHeaderUtils = apiHeaderUtils;
        this.apiResponse = apiResponse;
        this.httpRequest = httpRequest;
        this.role = role;
        this.errorCode = errorCode;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        ApiCurrentExecution currentExecution = ApiCurrentExecution.on(httpRequest);

        String authorizationRequest = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (currentExecution.getUserType().isPresent()) {
            final UserType userType = currentExecution.getUserType().or(UserType.NONE);
            if (!userType.isInRole(role)) {
                log.info("NOT {} profile detected : {}", role, userType);
                containerRequestContext.abortWith(apiResponse.unauthorize(apiHeaderUtils.parseAuthorization(authorizationRequest), ErrorResponse.of(ErrorResponse.DetailsCode.INVALID_PROFILE, errorCode)));
            }
        } else {
            containerRequestContext.abortWith(apiResponse.unauthorize(apiHeaderUtils.parseAuthorization(authorizationRequest), ErrorResponse.of(ErrorResponse.DetailsCode.AUTHENTICATION_REQUIRED, ErrorResponse.DetailsCode.INVALID_CREDENTIALS)));
        }

    }
}
