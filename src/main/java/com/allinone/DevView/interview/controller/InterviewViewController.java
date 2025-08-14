package com.allinone.DevView.interview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/interview")
public class InterviewViewController {

    @GetMapping("/settings")
    public String interviewSettingsPage() {
        return "interview/settings";
    }

    @GetMapping("/session")
    public String interviewSessionPage() {
        return "interview/session";
    }

    @GetMapping("/result/{interviewId}")
    public String interviewResultPage() {
        return "interview/result";
    }
}
