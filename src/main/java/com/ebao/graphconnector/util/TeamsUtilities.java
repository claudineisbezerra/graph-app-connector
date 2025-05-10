// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.ebao.graphconnector.util;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.microsoft.graph.authentication.BaseAuthenticationProvider;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.microsoft.graph.models.Team;

public class TeamsUtilities {
    private TeamsUtilities() {
        throw new IllegalStateException("Utility class. Don't instantiate");
    }

    /**
     * Take a subset of ID Token claims and put them into KV pairs for UI to display.
     * @param principal OidcUser (see TeamsController for details)
     * @return Map of filteredClaims
     */
    public static Map<String,String> filterClaims(OidcUser principal) {
        final String[] claimKeys = {"sub", "aud", "ver", "iss", "name", "oid", "preferred_username"};
        final List<String> includeClaims = Arrays.asList(claimKeys);

        Map<String,String> filteredClaims = new HashMap<>();
        includeClaims.forEach(claim -> {
            if (principal.getIdToken().getClaims().containsKey(claim)) {
                filteredClaims.put(claim, principal.getIdToken().getClaims().get(claim).toString());
            }
        });
        return filteredClaims;
    }

    /**
     * Take a few of the User properties obtained from the graph /me endpoint and put them into KV pairs for UI to display.
     * @param graphAuthorizedClient OAuth2AuthorizedClient created by AAD Boot starter. See the TeamsController class for details.
     * @return Map<String,String> select Key-Values from User object
     */
    public static Map<String, String> graphUserProperties(OAuth2AuthorizedClient graphAuthorizedClient) {
        final GraphServiceClient<?> graphServiceClient = TeamsUtilities.getGraphServiceClient(graphAuthorizedClient);
        final User user = graphServiceClient.me().buildRequest().get();
        Map<String, String> userProperties = new HashMap<>();

        if (user == null) {
            userProperties.put("Graph Error", "GraphSDK returned null User object.");
        } else {
            userProperties.put("Display Name", user.displayName);
            userProperties.put("Phone Number", user.mobilePhone);
            userProperties.put("City", user.city);
            userProperties.put("Given Name", user.givenName);
        }
        return userProperties;
    }

    /**
     * Sends a message to a specific channel in Microsoft Teams.
     *
     * @param graphAuthorizedClient OAuth2AuthorizedClient created by the AAD Boot Starter. Contains the access token.
     * @param teamId The ID of the team in Microsoft Teams.
     * @param channelId The ID of the channel in Microsoft Teams.
     * @param message The content of the message to be sent.
     */
    public static void sendMessageToTeamsChannel(OAuth2AuthorizedClient graphAuthorizedClient, String teamId, String channelId, String message) {
        GraphServiceClient<?> graphServiceClient = getGraphServiceClient(graphAuthorizedClient);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.body = new ItemBody();
        chatMessage.body.content = message;

        graphServiceClient.teams(teamId)
                .channels(channelId)
                .messages()
                .buildRequest()
                .post(chatMessage);
    }

    /**
     * getGraphServiceClient prepares and returns a graphServiceClient to make API calls to
     * Microsoft Graph. See docs for GraphServiceClient (GraphSDK for Java v3).
     *
     *
     * Since the app handles token acquisition through AAD boot starter, we can give GraphServiceClient
     * the ability to use this access token when it requires it. In order to do this, we must create a
     * custom AuthenticationProvider (GraphAuthenticationProvider, see below).
     * 
     * 
     * @param graphAuthorizedClient OAuth2AuthorizedClient created by AAD Boot starter. Used to surface the access token.
     * @return GraphServiceClient GraphServiceClient
     */
    
     public static GraphServiceClient<?> getGraphServiceClient(@Nonnull OAuth2AuthorizedClient graphAuthorizedClient) {
         return GraphServiceClient.builder()
                 .authenticationProvider(new GraphAuthenticationProvider(graphAuthorizedClient))
                 .buildClient();
     }

    /**
     * A private implementation of the `BaseAuthenticationProvider` interface
     * that provides an access token for authenticating requests made by the
     * `GraphServiceClient`. This class is used internally to inject the
     * access token into the headers of outgoing requests.
     */
    private static class GraphAuthenticationProvider
            extends BaseAuthenticationProvider {

        /**
         * The OAuth2AuthorizedClient containing the access token used for authentication.
         * This field is final to ensure it is immutable after initialization.
         */
        private final OAuth2AuthorizedClient graphAuthorizedClient;

        /**
         * Constructs a new instance of `GraphAuthenticationProvider` with the specified
         * `OAuth2AuthorizedClient`. This allows the `GraphServiceClient` to use the
         * access token for making authenticated requests to Microsoft Graph.
         *
         * @param graphAuthorizedClient The `OAuth2AuthorizedClient` created by the AAD Boot starter.
         *                              It provides the access token required for authentication.
         */
        public GraphAuthenticationProvider(@Nonnull OAuth2AuthorizedClient graphAuthorizedClient) {
           this.graphAuthorizedClient = graphAuthorizedClient;
        }

        /**
         * Retrieves the access token from the `OAuth2AuthorizedClient` and injects it into
         * the headers of the outgoing request made by the `GraphServiceClient`.
         *
         * @param requestUrl The URL of the outgoing request.
         * @return A `CompletableFuture` containing the access token as a string.
         */
        @Override
        public @Nonnull CompletableFuture<String> getAuthorizationTokenAsync(@Nonnull final URL requestUrl) {
            return CompletableFuture.completedFuture(graphAuthorizedClient.getAccessToken().getTokenValue());
        }
    }

    public static Map<String, Object> convertTeamToMap(Team team) {
        Map<String, Object> teamMap = new HashMap<>();
        teamMap.put("id", team.id);
        teamMap.put("displayName", team.displayName);
        return teamMap;
    }
}
