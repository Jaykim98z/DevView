package com.allinone.DevView.interview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for serving the main HTML pages of the interview feature.
 * This handles rendering the Thymeleaf templates.
 */
@Controller
@RequestMapping("/interview")
public class InterviewViewController {

    /**
     * Displays the interview settings page.
     * @return The path to the settings template.
     */
    @GetMapping("/settings")
    public String interviewSettingsPage() {
        return "interview/settings";
    }

    /**
     * Displays the main interview session page.
     * @return The path to the session template.
     */
    @GetMapping("/session")
    public String interviewSessionPage() {
        return "interview/session";
    }

    /**
     * Displays the interview result page.
     * @return The path to the result template.
     */
    @GetMapping("/result/{interviewId}")
    public String interviewResultPage() {
        return "interview/result";
    }
}
