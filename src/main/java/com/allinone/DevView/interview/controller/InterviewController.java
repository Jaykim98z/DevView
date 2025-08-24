package com.allinone.DevView.interview.controller;

import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.request.SubmitAnswerRequest;
import com.allinone.DevView.interview.dto.response.InterviewResponse;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.interview.dto.response.QuestionResponse;
import com.allinone.DevView.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling all interview-related API requests.
 */
@Tag(name = "Interview API", description = "면접 생성, 진행, 결과 조회 관련 API")
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
    @Operation(summary = "면접 세션 시작", description = "사용자 설정을 바탕으로 새로운 면접 세션을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "면접 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
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
    @Operation(summary = "면접 질문 요청", description = "진행중인 면접에 대한 AI 생성 질문 목록을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "질문 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 면접")
    })
    @PostMapping("/{interviewId}/questions")
    public ResponseEntity<List<QuestionResponse>> askQuestions(
            @Parameter(description = "질문을 요청할 면접의 ID") @PathVariable Long interviewId) {
        List<QuestionResponse> response = interviewService.askAndSaveQuestions(interviewId);

        return ResponseEntity.ok(response);
    }

    /**
     * Submits a batch of answers for an interview.
     * @param request The request DTO containing the list of answers.
     * @return An empty response with a 200 OK status.
     */
    @Operation(summary = "답변 일괄 제출", description = "진행중인 면접에 대한 모든 답변을 일괄 제출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "답변 제출 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 면접 또는 질문")
    })
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
    @Operation(summary = "면접 종료 및 분석 시작", description = "면접을 종료하고 AI 분석을 시작합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "면접 종료 및 분석 시작 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 면접")
    })
    @PostMapping("/{interviewId}/end")
    public ResponseEntity<InterviewResultResponse> endInterview(
            @Parameter(description = "종료할 면접의 ID") @PathVariable Long interviewId) {
        InterviewResultResponse response = interviewService.endInterview(interviewId);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the final result of a completed interview.
     * @param interviewId The ID of the interview to get the result for.
     * @return A DTO with the full interview result.
     */
    @Operation(summary = "면접 결과 조회", description = "완료된 면접의 최종 결과를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결과 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 면접 결과")
    })
    @GetMapping("/{interviewId}/results")
    public ResponseEntity<InterviewResultResponse> getInterviewResult(
            @Parameter(description = "결과를 조회할 면접의 ID") @PathVariable Long interviewId) {
        InterviewResultResponse response = interviewService.getInterviewResult(interviewId);

        return ResponseEntity.ok(response);
    }
}
