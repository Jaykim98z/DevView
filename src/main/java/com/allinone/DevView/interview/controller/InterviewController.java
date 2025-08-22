package com.allinone.DevView.interview.controller;

import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.request.SubmitAnswerRequest;
import com.allinone.DevView.interview.dto.response.InterviewResponse;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.interview.dto.response.QuestionResponse;
import com.allinone.DevView.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling all interview-related API requests.
 */
@Tag(name = "Interview", description = "Interview API")
@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;

    /**
     * Starts a new interview session based on user settings.
     * @param request The request DTO containing interview settings.
     * @return A DTO with the new interview's basic information.
     */
    @Operation(summary = "Start a new interview")
    @PostMapping("/start")
    public ResponseEntity<InterviewResponse> startInterview(@RequestBody StartInterviewRequest request) {
        InterviewResponse response = interviewService.startInterview(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Generates and retrieves a batch of questions for an ongoing interview.
     * @param interviewId The ID of the interview to generate questions for.
     * @return A list of question DTOs.
     */
    @Operation(summary = "Get a batch of questions")
    @PostMapping("/{interviewId}/questions")
    public ResponseEntity<List<QuestionResponse>> askQuestions(@PathVariable Long interviewId) {
        List<QuestionResponse> response = interviewService.askAndSaveQuestions(interviewId);

        return ResponseEntity.ok(response);
    }

    /**
     * Submits a batch of answers for an interview.
     * @param request The request DTO containing the list of answers.
     * @return An empty response with a 200 OK status.
     */
    @Operation(summary = "Submit a batch of answers")
    @PostMapping("/answers")
    public ResponseEntity<Void> submitAnswers(@RequestBody SubmitAnswerRequest request) {
        interviewService.submitAnswers(request);

        return ResponseEntity.ok().build();
    }

    /**
     * Ends an interview and triggers the AI analysis.
     * @param interviewId The ID of the interview to end.
     * @return A DTO with the initial interview result (recommendations are generated asynchronously).
     */
    @Operation(summary = "End an interview and start analysis")
    @PostMapping("/{interviewId}/end")
    public ResponseEntity<InterviewResultResponse> endInterview(@PathVariable Long interviewId) {
        InterviewResultResponse response = interviewService.endInterview(interviewId);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the final result of a completed interview.
     * @param interviewId The ID of the interview to get the result for.
     * @return A DTO with the full interview result.
     */
    @Operation(summary = "Get interview result")
    @GetMapping("/{interviewId}/results")
    public ResponseEntity<InterviewResultResponse> getInterviewResult(@PathVariable Long interviewId) {
        InterviewResultResponse response = interviewService.getInterviewResult(interviewId);

        return ResponseEntity.ok(response);
    }
}
