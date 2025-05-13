// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.microsoft.aad.msal4j.*;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class ClientCredentialGrant {

    private static String instance;
    private static String tenantId;
    private static String clientId;
    private static String secret;
    private static String scope;


    public static void main(String args[]) throws Exception{

        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        instance = properties.getProperty("INSTANCE");
        clientId = properties.getProperty("CLIENT_ID");
        secret = properties.getProperty("SECRET");

        // With client credentials flows the scope is ALWAYS of the shape "resource/.default", as the
        // application permissions need to be set statically (in the portal), and then granted by a tenant administrator
        scope = properties.getProperty("SCOPES");

        // A multi-tenanted app can have a list of these. There will be 1 token per tenant.
        // Multi-tenanted services might feature millions of tenants so a token cache with size eviction is needed
        // to avoid out of memory issues
        String tenantId = properties.getProperty("TENANT_ID");


        try {

//            IAuthenticationResult result = getAccessTokenByClientCredentialGrant(instance, clientId, tenantId, secret, scope);
//
//            // If you try to fetch the same token again, it will hit the token cache and it will be much faster.
//            // In case AAD has an outage, cached tokens are still available, increasing your app's resiliency.
//            result = getAccessTokenByClientCredentialGrant(instance, clientId, tenantId, secret, scope);
//
//
//
//            String usersListFromGraph = getUsersListFromGraph(result.accessToken());
//            System.out.println("Users in the Tenant = " + usersListFromGraph);
//            System.out.println("Press any key to exit ...");
//            System.in.read();


            // Obter o token de acesso
            IAuthenticationResult result = getAccessTokenByClientCredentialGrant(instance, clientId, tenantId, secret, scope);
            String accessToken = result.accessToken();

            // Definir os parâmetros do time e canal
            String teamName = "InsureMO Brazil"; // Fixed team name
            String channelName = "Workflow Notifications"; // Fixed Channel name
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String message = "Message sent at " + timestamp + ": This is a workflow notification message with Application Permission.";
            String teamId = "f53d4c96-227e-42bb-b688-1b292ff8bc38";
            String channelId = "19:d909e734d5dd4500a83c069c6e62b0f9@thread.tacv2";

            System.out.println("Team ID: " + teamId);
            System.out.println("Channel ID: " + channelId);
            System.out.println("Access Token: " + accessToken);
            System.out.println("Message: " + message);

            // Enviar a mensagem para o canal
            String response = sendMessageToChannel(accessToken, teamId, channelId, message);
            System.out.println("Resposta da API: " + response);
        } catch(Exception ex){
            System.out.println("Oops! We have an exception of type - " + ex.getClass());
            System.out.println("Exception message - " + ex.getMessage());
            throw ex;
        }
    }


    private static IAuthenticationResult getAccessTokenByClientCredentialGrant(String instance, String clientId, String tenantId, String secret, String scopes) throws Exception {

        // Configurar o token cache
        String cacheKey = clientId + "_" + tenantId + "_AppTokenCache";
        MemoryTokenCacheWithEviction memoryTokenCacheWithEviction = new MemoryTokenCacheWithEviction(cacheKey);

        ConfidentialClientApplication cca = ConfidentialClientApplication.builder(
                        clientId,
                        ClientCredentialFactory.createFromSecret(secret))
                .authority(instance + tenantId)
                .setTokenCacheAccessAspect(memoryTokenCacheWithEviction)
                .build();

        // Configurar os escopos
        String[] scopeArray = scopes.split(",\\s*"); // Divide os escopos por vírgula e espaço
        String scopeString = String.join(" ", scopeArray); // Junta os escopos com espaço

        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
                Collections.singleton(scopeString))
                .build();

        Stopwatch stopwatch = Stopwatch.createStarted();
        CompletableFuture<IAuthenticationResult> future = cca.acquireToken(clientCredentialParam);
        IAuthenticationResult result = future.get();

        System.out.println("Time to fetch the token: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return result;
    }

//    private static IAuthenticationResult getAccessTokenByClientCredentialGrant(String instance, String clientId, String tenantId, String secret, String scope) throws Exception {
//
//        // Setup the token cache first. It is configured to allow 100k token entries, which at 2-3KB per entry, will take under 500MB of memory
//        // Note that size calculations are approximate and depend on the JVM
//        String cacheKey = clientId + "_" + tenantId + "_AppTokenCache";
//        MemoryTokenCacheWithEviction memoryTokenCacheWithEviction = new MemoryTokenCacheWithEviction(cacheKey);
//
//        ConfidentialClientApplication cca = ConfidentialClientApplication.builder(
//                        clientId,
//                        ClientCredentialFactory.createFromSecret(secret))
//                .authority(instance + tenantId)
//                .setTokenCacheAccessAspect(memoryTokenCacheWithEviction)
//                .build();
//
//        // Important: point the CCA object to a token cache
//
//        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
//                Collections.singleton(scope))
//                .build();
//
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        CompletableFuture<IAuthenticationResult> future = cca.acquireToken(clientCredentialParam);
//        IAuthenticationResult result = future.get();
//
//        System.out.println("Time to fetch the token: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
//
//        return result;
//    }

    private static String getUsersListFromGraph(String accessToken) throws IOException {
        URL url = new URL("https://graph.microsoft.com/v1.0/users");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept","application/json");

        int httpResponseCode = conn.getResponseCode();
        if(httpResponseCode == HTTPResponse.SC_OK) {

            StringBuilder response;
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))){

                String inputLine;
                response = new StringBuilder();
                while (( inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            return response.toString();
        } else {
            return String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, conn.getResponseMessage());
        }
    }

    private static String sendMessageToChannel(String accessToken, String teamId, String channelId, String message) throws IOException {
//        URL url = new URL("https://graph.microsoft.com/v1.0/teams/" + teamId + "/channels/" + channelId + "/messages");
        URL url = new URL("https://graph.microsoft.com/beta/teams/" + teamId + "/channels/" + channelId + "/messages");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String payload = String.format("{\"body\": {\"content\": \"%s\"}}", message);

        try (OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(payload.getBytes());
            outputStream.flush();
        }

        int httpResponseCode = conn.getResponseCode();
        if (httpResponseCode == HTTPResponse.SC_CREATED) {
            StringBuilder response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            return response.toString();
        } else {
            return String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, conn.getResponseMessage());
        }
    }

    private static String getTeamInfoByName(String accessToken, String teamName) throws IOException {
        URL url = new URL("https://graph.microsoft.com/v1.0/teams?$filter=displayName eq '" + teamName + "'");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int httpResponseCode = conn.getResponseCode();
        if (httpResponseCode == HTTPResponse.SC_OK) {
            StringBuilder response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            return response.toString();
        } else {
            return String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, conn.getResponseMessage());
        }
    }

    private static String getChannelInfoByTeamIdAndChannelName(String accessToken, String teamId, String channelName) throws IOException {
        URL url = new URL("https://graph.microsoft.com/v1.0/teams/" + teamId + "/channels?$filter=displayName eq '" + channelName + "'");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int httpResponseCode = conn.getResponseCode();
        if (httpResponseCode == HTTPResponse.SC_OK) {
            StringBuilder response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            return response.toString();
        } else {
            return String.format("Connection returned HTTP code: %s with message: %s",
                    httpResponseCode, conn.getResponseMessage());
        }
    }
}
