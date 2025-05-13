package com.ebao.graphconnector.service;

import com.ebao.graphconnector.config.SecurityProperties;
import com.microsoft.aad.msal4j.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Service
public class AuthenticationService {

    private final SecurityProperties props;

    public AuthenticationService(SecurityProperties props) {
        this.props = props;
    }

    public String getAccessToken() throws Exception {
        String cacheKey = props.getClientId() + "_" + props.getTenantId() + "_AppTokenCache";
        MemoryTokenCacheWithEviction cache = new MemoryTokenCacheWithEviction(cacheKey);

        ConfidentialClientApplication app = ConfidentialClientApplication.builder(
                        props.getClientId(),
                        ClientCredentialFactory.createFromSecret(props.getSecret()))
                .authority(props.getInstance() + props.getTenantId())
                .setTokenCacheAccessAspect(cache)
                .build();

        ClientCredentialParameters params = ClientCredentialParameters.builder(
                Collections.singleton(props.getScopes())).build();

        CompletableFuture<IAuthenticationResult> future = app.acquireToken(params);
        return future.get().accessToken();
    }
}
