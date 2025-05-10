// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.ebao.graphconnector.controller;

import com.ebao.graphconnector.util.TeamsHelper;
import com.ebao.graphconnector.util.TeamsUtilities;
import com.microsoft.graph.models.Team;
import com.microsoft.graph.models.Channel;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class SampleController {

    /**
     * Add HTML partial fragment from /templates/content folder to request and serve base html
     * @param model Model used for placing user param and bodyContent param in request before serving UI.
     * @param fragment used to determine which partial to put into UI
     */
    private String hydrateUI(Model model, String fragment) {
        model.addAttribute("bodyContent", String.format("content/%s.html", fragment));
        return "base"; // base.html in /templates folder
    }

    /**
     *  Sign in status endpoint
     *  The page demonstrates sign-in status. For full details, see the src/main/webapp/content/status.html file.
     * 
     * @param model Model used for placing bodyContent param in request before serving UI.
     * @return String the UI.
     */
    @GetMapping(value = {"/", "sign_in_status", "/index"})
    public String status(Model model) {
        return hydrateUI(model, "status");
    }

    /**
     *  Token details endpoint
     *  Demonstrates how to extract and make use of token details
     *  For full details, see method: TeamsUtilities.filterclaims(OidcUser principal)
     * 
     * @param model Model used for placing claims param and bodyContent param in request before serving UI.
     * @param principal OidcUser this object contains all ID token claims about the user. See TeamsUtilities file.
     * @return String the UI.
     */
    @GetMapping(path = "/token_details")
    public String tokenDetails(Model model, @AuthenticationPrincipal OidcUser principal) {
        model.addAttribute("claims", TeamsUtilities.filterClaims(principal));
        return hydrateUI(model, "token");
    }

    /**
     *  Call Graph endpoint
     *  Demonstrates how to utilize OAuth2AuthorizedClient.
     *  Passes that client over to TeamsUtilities.graphUserProperties, which creates a GraphServiceClient (GraphSDK v3)
     *  For full details, see method: TeamsUtilities.graphUserProperties(OAuth2AuthorizedClient graphAuthorizedClient)
     * 
     * @param model Model used for placing user param and bodyContent param in request before serving UI.
     * @param graphAuthorizedClient OAuth2AuthorizedClient this object contains Graph Access Token. See TeamsUtilities file.
     * @return String the UI.
     */
    @GetMapping(path = "/call_graph")
    public String callGraph(Model model, @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphAuthorizedClient) {
        model.addAttribute("user", TeamsUtilities.graphUserProperties(graphAuthorizedClient));
        return hydrateUI(model, "graph");
    }

    // survey endpoint - did the sample address your needs?
    // not an integral a part of this tutorial.
    @GetMapping(path = "/survey")
    public String survey(Model model) {
        return hydrateUI(model, "survey");
    }


    /**
     * Endpoint to retrieve the properties of a Microsoft Teams team by its name.
     *
     * @param model The model used to add attributes for the UI.
     * @param teamName The name of the team to retrieve properties for.
     * @param graphAuthorizedClient The OAuth2AuthorizedClient containing the access token.
     * @return The UI page displaying the team properties or an error message.
     */
    @GetMapping(path = "/get_team_properties")
    public String getTeamProperties(Model model,
                                    @RequestParam String teamName,
                                    @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphAuthorizedClient) {
        try {
            Team team = TeamsHelper.getTeamByName(graphAuthorizedClient, teamName);
            if (team == null) {
                model.addAttribute("status", "Team not found: " + teamName);
            } else {
                model.addAttribute("status", "Team found!");
                model.addAttribute("teamId", team.id );
                model.addAttribute("teamDisplayName", team.displayName );
                model.addAttribute("teamChannels", team.channels );
                model.addAttribute("teamGroup", team.group );
            }

        } catch (Exception e) {
            model.addAttribute("status", "Error retrieving team properties: " + e.getMessage());
        }

        System.out.println("SampleController: Retrieved /get_team_properties > model = " + model);
        return hydrateUI(model, "team_properties_status");
    }

    /**
     * Endpoint to retrieve the properties of a Microsoft Teams channel by its name and the team it belongs to.
     *
     * @param model The model used to add attributes for the UI.
     * @param teamName The name of the team containing the channel.
     * @param channelName The name of the channel to retrieve properties for.
     * @param graphAuthorizedClient The OAuth2AuthorizedClient containing the access token.
     * @return The UI page displaying the channel properties or an error message.
     */
    @GetMapping(path = "/get_channel_properties")
    public String getChannelProperties(Model model,
                                       @RequestParam String teamName,
                                       @RequestParam String channelName,
                                       @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphAuthorizedClient) {
        try {
            Team team = TeamsHelper.getTeamByName(graphAuthorizedClient, teamName);
            if (team == null) {
                model.addAttribute("status", "Team not found: " + teamName);
                return hydrateUI(model, "channel_properties_status");
            }

            Channel channel = TeamsHelper.getChannelByName(graphAuthorizedClient, team.id, channelName);
            if (channel == null) {
                model.addAttribute("status", "Channel not found in team: " + channelName);
            } else {
                model.addAttribute("status", "Channel found!");
                model.addAttribute("teamId", team.id );
                model.addAttribute("teamDisplayName", team.displayName );
                model.addAttribute("teamChannels", team.channels );
                model.addAttribute("teamGroup", team.group );

                model.addAttribute("channelId", channel.id);
                model.addAttribute("channelDisplayName", channel.displayName );

            }
        } catch (Exception e) {
            model.addAttribute("status", "Error retrieving channel properties: " + e.getMessage());
        }
        return hydrateUI(model, "channel_properties_status");
    }

    /**
     * Endpoint to send a message to a specific Microsoft Teams channel.
     *
     * @param model Model used to add attributes for the UI.
     * @param graphAuthorizedClient OAuth2AuthorizedClient created by the AAD Boot Starter. Contains the access token.
     * @return String the UI page to display the status of the operation.
     */
    @GetMapping(path = "/graph_send_teams_message")
    public String sendTeamsMessage(Model model, @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphAuthorizedClient) {
        String teamName = "InsureMO Brazil"; // Fixed team name
        String channelName = "Workflow Notifications"; // Fixed Channel name
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = "Message sent at " + timestamp + ": This is a workflow notification message."; // Dynamic message

        try {
            // Get teamId by name
            String teamId = TeamsHelper.getTeamIdByName(graphAuthorizedClient, teamName);
            System.out.println("Debug: Retrieved teamId - DisplayName = " + teamId); // Debugging output
            if (teamId == null) {
                model.addAttribute("status", "Team not found: " + teamName);
                return hydrateUI(model, "teams_message_status");
            }

            // Get channelId by name
            String channelId = TeamsHelper.getChannelIdByName(graphAuthorizedClient, teamId, channelName);
            System.out.println("Debug: Retrieved channelId = " + channelId); // Debugging output
            if (channelId == null) {
                model.addAttribute("status", "Channel not found in team: " + channelName);
                return hydrateUI(model, "teams_message_status");
            }

            // Send message to the channel
            TeamsUtilities.sendMessageToTeamsChannel(graphAuthorizedClient, teamId, channelId, message);

            model.addAttribute("status", "Message sent successfully!");
            model.addAttribute("teamId", teamId);
            model.addAttribute("channelId", channelId);
            model.addAttribute("message", message);

        } catch (Exception e) {
            model.addAttribute("status", "Error sending message: " + e.getMessage());
        }

        return hydrateUI(model, "teams_message_status");
    }
}
