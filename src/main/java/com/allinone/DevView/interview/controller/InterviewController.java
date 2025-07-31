package com.allinone.DevView.interview.controller;

import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.response.InterviewResponse;
import com.allinone.DevView.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<InterviewResponse> startInterview(@RequestBody StartInterviewRequest request) {
        InterviewResponse response = interviewService.startInterview(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
