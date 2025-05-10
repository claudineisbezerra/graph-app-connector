package com.ebao.graphconnector.util;

import com.microsoft.graph.models.Team;
import com.microsoft.graph.models.Channel;
import com.microsoft.graph.requests.GraphServiceClient;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public class TeamsHelper {

    /**
     * Retrieves all properties of a Microsoft Teams team by its name.
     *
     * @param graphAuthorizedClient OAuth2AuthorizedClient containing the access token.
     * @param teamName The name of the team.
     * @return The Team object containing all properties, or null if not found.
     */
    public static Team getTeamByName(OAuth2AuthorizedClient graphAuthorizedClient, String teamName) {
        if (graphAuthorizedClient == null) {
            throw new IllegalArgumentException("OAuth2AuthorizedClient cannot be null.");
        }

        GraphServiceClient<?> graphClient = TeamsUtilities.getGraphServiceClient(graphAuthorizedClient);

        try {
            var teamsResponse = graphClient.me().joinedTeams().buildRequest().get();
            System.out.println("TeamsHelper: Retrieved teamsResponse = " + teamsResponse);
            if (teamsResponse != null) {
                for (Team team : teamsResponse.getCurrentPage()) {
                    System.out.println("TeamsHelper: Retrieved team = " + team);
                    System.out.println("TeamsHelper: Retrieved team.displayName = " + team.displayName);
                    if (team.displayName != null && team.displayName.equalsIgnoreCase(teamName)) {
                        return team;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving team properties: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Retrieves the team ID by its name.
     *
     * @param graphAuthorizedClient OAuth2AuthorizedClient containing the access token.
     * @param teamName The name of the team.
     * @return The ID of the team, or null if not found.
     */
    public static String getTeamIdByName(OAuth2AuthorizedClient graphAuthorizedClient, String teamName) {
        if (graphAuthorizedClient == null) {
            throw new IllegalArgumentException("OAuth2AuthorizedClient cannot be null.");
        }

        GraphServiceClient<?> graphClient = TeamsUtilities.getGraphServiceClient(graphAuthorizedClient);

        try {
            var teamsResponse = graphClient.me().joinedTeams().buildRequest().get();
            if (teamsResponse != null) {
                for (Team team : teamsResponse.getCurrentPage()) {
                    System.out.println("TeamsHelper: Retrieved teamId - DisplayName = " + team.id + " - " + team.displayName);
                    if (team.displayName != null && team.displayName.equalsIgnoreCase(teamName)) {
                        return team.id;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving team ID: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Retrieves all properties of a Microsoft Teams channel by its name within a specific team.
     *
     * @param graphAuthorizedClient OAuth2AuthorizedClient containing the access token.
     * @param teamId The ID of the team.
     * @param channelName The name of the channel.
     * @return The Channel object containing all properties, or null if not found.
     */
    public static Channel getChannelByName(OAuth2AuthorizedClient graphAuthorizedClient, String teamId, String channelName) {
        if (graphAuthorizedClient == null) {
            throw new IllegalArgumentException("OAuth2AuthorizedClient cannot be null.");
        }

        GraphServiceClient<?> graphClient = TeamsUtilities.getGraphServiceClient(graphAuthorizedClient);

        try {
            var channelResponse = graphClient.teams(teamId).channels().buildRequest().get();
            System.out.println("TeamsHelper: Retrieved channelResponse = " + channelResponse);
            if (channelResponse != null) {
                for (Channel channel : channelResponse.getCurrentPage()) {
                    System.out.println("TeamsHelper: Retrieved channel = " + channel);
                    System.out.println("TeamsHelper: Retrieved channel.displayName = " + channel.displayName);
                    if (channel.displayName != null && channel.displayName.equalsIgnoreCase(channelName)) {
                        return channel;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving channel properties: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Retrieves the channel ID by its name within a specific team.
     *
     * @param graphAuthorizedClient OAuth2AuthorizedClient containing the access token.
     * @param teamId The ID of the team.
     * @param channelName The name of the channel.
     * @return The ID of the channel, or null if not found.
     */
    public static String getChannelIdByName(OAuth2AuthorizedClient graphAuthorizedClient, String teamId, String channelName) {
        if (graphAuthorizedClient == null) {
            throw new IllegalArgumentException("OAuth2AuthorizedClient cannot be null.");
        }

        GraphServiceClient<?> graphClient = TeamsUtilities.getGraphServiceClient(graphAuthorizedClient);

        try {
            var channelResponse = graphClient.teams(teamId).channels().buildRequest().get();

            if (channelResponse != null) {
                for (Channel channel : channelResponse.getCurrentPage()) {
                    System.out.println("TeamsHelper: Retrieved teamId - channelId - DisplayName = " + teamId + " - " + channel.id + " - " + channel.displayName);
                    if (channel.displayName != null && channel.displayName.equalsIgnoreCase(channelName)) {
                        return channel.id;
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving channel ID: " + e.getMessage(), e);
        }

        return null;
    }
}