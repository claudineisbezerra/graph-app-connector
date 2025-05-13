package com.ebao.graphconnector.controller;

import com.ebao.graphconnector.config.SecurityProperties;
import com.ebao.graphconnector.service.AuthenticationService;
import com.ebao.graphconnector.service.GraphService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
public class TeamsController {

    private final AuthenticationService authService;
    private final GraphService graphService;
    private final SecurityProperties props;

    public TeamsController(AuthenticationService authService, GraphService graphService, SecurityProperties props) {
        this.authService = authService;
        this.graphService = graphService;
        this.props = props;
    }

    @PostMapping("/send_teams_message")
    public String sendMessage() throws Exception {
        String accessToken = authService.getAccessToken();

        System.out.println();
        System.out.println("Access Token: " + accessToken);
        System.out.println("Sending message to Teams channel...");
        System.out.println("Team ID: " + props.getTeamId());
        System.out.println("Channel ID: " + props.getChannelId());
        System.out.println();

        String message = "Notification with application permission sent at: " + java.time.LocalDateTime.now();
        return graphService.sendMessageToChannel(accessToken, props.getTeamId(), props.getChannelId(), message);
    }
}
