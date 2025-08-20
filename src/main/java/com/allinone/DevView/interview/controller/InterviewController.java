package com.allinone.DevView.interview.controller;

import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.request.SubmitAnswerRequest;
import com.allinone.DevView.interview.dto.response.InterviewResponse;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.interview.dto.response.QuestionResponse;
import com.allinone.DevView.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* ▼ 추가: 로그인 사용자 식별을 위해 Authentication, UserRepository 사용 */
import org.springframework.security.core.Authentication;                    // 추가
import com.allinone.DevView.user.repository.UserRepository;            // 추가
import com.allinone.DevView.user.entity.User;                          // 추가
/* ▲ 추가 끝 */

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;

    /* ▼ 추가: 이메일로 로그인 사용자를 찾기 위해 UserRepository 주입 */
    private final UserRepository userRepository; // 추가
    /* ▲ 추가 끝 */

    @PostMapping("/start")
    public ResponseEntity<InterviewResponse> startInterview(@RequestBody StartInterviewRequest request) {
        InterviewResponse response = interviewService.startInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{interviewId}/questions")
    public ResponseEntity<List<QuestionResponse>> askQuestions(@PathVariable Long interviewId) {
        List<QuestionResponse> response = interviewService.askAndSaveQuestions(interviewId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/answers")
    public ResponseEntity<Void> submitAnswers(@RequestBody SubmitAnswerRequest request) {
        interviewService.submitAnswers(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{interviewId}/end")
    public ResponseEntity<InterviewResultResponse> endInterview(@PathVariable Long interviewId) {
        InterviewResultResponse response = interviewService.endInterview(interviewId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{interviewId}/results")
    public ResponseEntity<InterviewResultResponse> getInterviewResult(@PathVariable Long interviewId) {
        InterviewResultResponse response = interviewService.getInterviewResult(interviewId);
        return ResponseEntity.ok(response);
    }

    /* ===========================
       추가 1) 최신 결과 자동 불러오기
       - 로그인(세션) 사용자 이메일(authentication.getName())로 User 조회
       - GET /api/v1/interviews/results/latest
       =========================== */
    @GetMapping("/results/latest")
    public ResponseEntity<?> getLatestResult(Authentication authentication) { // 추가
        if (authentication == null || !authentication.isAuthenticated()) {     // 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();     // 추가
        }
        String email = authentication.getName();                                // 추가: 보안 컨텍스트의 name을 이메일로 사용
        User user = userRepository.findByEmail(email).orElse(null);            // 추가
        if (user == null) {                                                    // 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();     // 추가
        }

        InterviewResultResponse dto = interviewService.getLatestResultByUser(user.getUserId()); // 추가
        if (dto == null) return ResponseEntity.noContent().build();                              // 추가: 204(최근 결과 없음)
        return ResponseEntity.ok(dto);                                                          // 추가
    }

    /* ===========================
       추가 2) 내 결과 목록(선택용)
       - 페이징 목록(요약 DTO) 반환
       - GET /api/v1/interviews/my-results?page=&size=
       =========================== */
    @GetMapping("/my-results")
    public ResponseEntity<?> getMyResults(                               // 추가
                                                                         Authentication authentication,                                // 추가
                                                                         @RequestParam(defaultValue = "0") int page,                   // 추가
                                                                         @RequestParam(defaultValue = "10") int size                   // 추가
    ) {
        if (authentication == null || !authentication.isAuthenticated()) { // 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 추가
        }
        String email = authentication.getName();                           // 추가
        User user = userRepository.findByEmail(email).orElse(null);        // 추가
        if (user == null) {                                                // 추가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 추가
        }

        return ResponseEntity.ok(interviewService.getMyResults(user.getUserId(), page, size)); // 추가
    }

    /* ===========================
       추가 3) 단건 상세 조회(선택 후 채움)
       - GET /api/v1/interviews/results/{resultId}
       =========================== */
    @GetMapping("/results/{resultId}")
    public ResponseEntity<?> getResultByResultId(@PathVariable Long resultId) { // 추가
        InterviewResultResponse dto = interviewService.getResultByResultId(resultId);          // 추가
        return (dto == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);     // 추가
    }
}
