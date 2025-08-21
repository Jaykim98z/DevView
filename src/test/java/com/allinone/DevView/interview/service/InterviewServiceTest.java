package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.enums.CareerLevel;
import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.common.enums.InterviewType;
import com.allinone.DevView.common.enums.JobPosition;
import com.allinone.DevView.interview.dto.gemini.GeminiAnalysisResponseDto;
import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.request.SubmitAnswerRequest;
import com.allinone.DevView.interview.dto.response.InterviewResponse;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.interview.dto.response.QuestionResponse;
import com.allinone.DevView.interview.entity.*;
import com.allinone.DevView.interview.repository.InterviewAnswerRepository;
import com.allinone.DevView.interview.repository.InterviewQuestionRepository;
import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;

    @Mock
    private InterviewAnswerRepository interviewAnswerRepository;

    @Mock
    private InterviewResultRepository interviewResultRepository;

    @Mock
    private ExternalAiApiService gemini;

    @Mock
    private RecommendationGenerationService recommendationGenerationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InterviewService interviewService;

    @Test
    @DisplayName("면접 시작 - 성공")
    void startInterview_success() {
        // given
        StartInterviewRequest request = new StartInterviewRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "interviewType", InterviewType.TECHNICAL);
        ReflectionTestUtils.setField(request, "jobPosition", JobPosition.BACKEND);
        ReflectionTestUtils.setField(request, "careerLevel", CareerLevel.JUNIOR);
        ReflectionTestUtils.setField(request, "questionCount", 5);
        ReflectionTestUtils.setField(request, "durationMinutes", 15);

        User user = User.builder().username("testuser").build();
        Interview savedInterview = Interview.builder()
                .user(user)
                .interviewType(request.getInterviewType())
                .jobPosition(request.getJobPosition())
                .careerLevel(request.getCareerLevel())
                .build();
        ReflectionTestUtils.setField(savedInterview, "id", 10L);

        given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));
        given(interviewRepository.save(any(Interview.class))).willReturn(savedInterview);

        // when
        InterviewResponse response = interviewService.startInterview(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInterviewId()).isEqualTo(10L);
        assertThat(response.getInterviewType()).isEqualTo(InterviewType.TECHNICAL);
        assertThat(response.getJobPosition()).isEqualTo(JobPosition.BACKEND);
        assertThat(response.getCareerLevel()).isEqualTo(CareerLevel.JUNIOR);
    }

    @Test
    @DisplayName("면접 질문 저장 - 성공")
    void saveQuestion_success() {
        // given
        // 테스트에 필요한 객체들을 준비합니다.
        User user = User.builder().userId(1L).build();
        Interview mockInterview = Interview.builder()
                .id(1L)
                .user(user)
                .jobPosition(JobPosition.BACKEND)
                .build();
        String questionText = "What is SOLID?";
        InterviewQuestion savedQuestion = InterviewQuestion.builder()
                .id(100L)
                .interview(mockInterview)
                .text(questionText)
                .build();

        given(interviewQuestionRepository.save(any(InterviewQuestion.class))).willReturn(savedQuestion);

        // when
        QuestionResponse response = interviewService.saveQuestion(mockInterview, questionText);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getQuestionId()).isEqualTo(100L);
        assertThat(response.getText()).isEqualTo(questionText);
    }

    @Test
    @DisplayName("여러 답변 제출 - 성공")
    void submitAnswers_success() {
        // given
        SubmitAnswerRequest.AnswerItem item1 = new SubmitAnswerRequest.AnswerItem();
        ReflectionTestUtils.setField(item1, "questionId", 1L);
        ReflectionTestUtils.setField(item1, "answerText", "Answer 1");

        SubmitAnswerRequest.AnswerItem item2 = new SubmitAnswerRequest.AnswerItem();
        ReflectionTestUtils.setField(item2, "questionId", 2L);
        ReflectionTestUtils.setField(item2, "answerText", "Answer 2");

        SubmitAnswerRequest request = new SubmitAnswerRequest();
        ReflectionTestUtils.setField(request, "answers", List.of(item1, item2));

        InterviewQuestion question1 = InterviewQuestion.builder().id(1L).build();
        InterviewQuestion question2 = InterviewQuestion.builder().id(2L).build();
        given(interviewQuestionRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(question1, question2));

        // when
        interviewService.submitAnswers(request);

        // then
        ArgumentCaptor<List<InterviewAnswer>> captor = ArgumentCaptor.forClass(List.class);
        verify(interviewAnswerRepository).saveAll(captor.capture());

        List<InterviewAnswer> savedAnswers = captor.getValue();
        assertThat(savedAnswers).hasSize(2);
        assertThat(savedAnswers.get(0).getAnswerText()).isEqualTo("Answer 1");
        assertThat(savedAnswers.get(1).getQuestion().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("면접 종료 - 성공")
    void endInterview_success() throws Exception {
        // given
        Long interviewId = 1L;
        Interview mockInterview = Interview.builder()
                .id(interviewId)
                .questions(new ArrayList<>())
                .jobPosition(JobPosition.BACKEND)
                .careerLevel(CareerLevel.JUNIOR)
                .build();

        List<GeminiAnalysisResponseDto.DetailedFeedbackItem> mockFeedbackItems = List.of(
                new GeminiAnalysisResponseDto.DetailedFeedbackItem("Question 1?", "Answer 1.", "Feedback 1.")
        );
        GeminiAnalysisResponseDto mockAnalysis = new GeminiAnalysisResponseDto(
                85, "Summary", 90, 80, 88, 92, List.of("Java"), mockFeedbackItems
        );
        String fakeAiResponseJson = new ObjectMapper().writeValueAsString(mockAnalysis);

        InterviewResult mockResult = InterviewResult.builder()
                .id(1L)
                .interview(mockInterview)
                .totalScore(85)
                .grade(Grade.B)
                .feedback(fakeAiResponseJson)
                .build();

        given(interviewRepository.findByIdWithQuestions(interviewId)).willReturn(Optional.of(mockInterview));
        given(gemini.generateContent(any(String.class))).willReturn(fakeAiResponseJson);
        given(objectMapper.readValue(any(String.class), eq(GeminiAnalysisResponseDto.class))).willReturn(mockAnalysis);
        given(interviewResultRepository.save(any(InterviewResult.class))).willReturn(mockResult);
        doNothing().when(recommendationGenerationService).generateAndSaveRecommendations(any(Long.class), any(List.class));

        // when
        InterviewResultResponse response = interviewService.endInterview(interviewId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFeedback()).isEqualTo(fakeAiResponseJson);

        verify(recommendationGenerationService, times(1)).generateAndSaveRecommendations(mockResult.getId(), mockAnalysis.keywords());
    }

    @Test
    @DisplayName("면접 결과 조회 - 성공")
    void getInterviewResult_success() {
        // given
        Long interviewId = 1L;
        Interview mockInterview = Interview.builder().id(interviewId).build();
        InterviewResult mockResult = InterviewResult.builder()
                .id(1L)
                .interview(mockInterview)
                .totalScore(85)
                .grade(Grade.B)
                .feedback("{\"summary\":\"Great job!\"}")
                .build();

        given(interviewResultRepository.findByInterviewId(interviewId)).willReturn(Optional.of(mockResult));

        // when
        InterviewResultResponse response = interviewService.getInterviewResult(interviewId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInterviewId()).isEqualTo(interviewId);
        assertThat(response.getResultId()).isEqualTo(1L);
    }
}
