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
import com.allinone.DevView.mypage.repository.UserProfileRepository;
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
    private UserProfileRepository userProfileRepository;

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
    @DisplayName("질문 요청 및 저장 - 성공")
    void askAndSaveQuestions_success() {
        // given
        Long interviewId = 1L;
        User mockUser = User.builder().userId(1L).build();
        Interview mockInterview = Interview.builder()
                .user(mockUser)
                .jobPosition(JobPosition.BACKEND)
                .careerLevel(CareerLevel.JUNIOR)
                .questionCount(3)
                .interviewType(InterviewType.TECHNICAL)
                .build();

        List<String> fakeQuestions = List.of("Question 1", "Question 2", "Question 3");

        given(interviewRepository.findById(interviewId)).willReturn(Optional.of(mockInterview));
        given(userProfileRepository.findByUserUserId(anyLong())).willReturn(Optional.empty());
        given(gemini.getQuestionFromAi(
                anyString(),
                anyString(),
                anyInt(),
                any(InterviewType.class),
                any()
        )).willReturn(fakeQuestions);

        // when
        List<QuestionResponse> response = interviewService.askAndSaveQuestions(interviewId);

        // then
        ArgumentCaptor<String> jobPosCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> careerLvlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<InterviewType> typeCaptor = ArgumentCaptor.forClass(InterviewType.class);
        ArgumentCaptor<String> introCaptor = ArgumentCaptor.forClass(String.class);

        verify(gemini).getQuestionFromAi(jobPosCaptor.capture(), careerLvlCaptor.capture(), countCaptor.capture(), typeCaptor.capture(), introCaptor.capture());

        System.out.println("Actual jobPosition: " + jobPosCaptor.getValue());
        System.out.println("Actual careerLevel: " + careerLvlCaptor.getValue());
        System.out.println("Actual questionCount: " + countCaptor.getValue());
        System.out.println("Actual interviewType: " + typeCaptor.getValue());
        System.out.println("Actual selfIntroduction: " + introCaptor.getValue());
    }

    @Test
    @DisplayName("여러 답변 제출 - 성공")
    void submitAnswers_success() {
        // given
        SubmitAnswerRequest.AnswerItem item1 = new SubmitAnswerRequest.AnswerItem();
        ReflectionTestUtils.setField(item1, "questionId", 1L);
        ReflectionTestUtils.setField(item1, "answerText", "Answer 1");

        SubmitAnswerRequest request = new SubmitAnswerRequest();
        ReflectionTestUtils.setField(request, "interviewId", 1L);
        ReflectionTestUtils.setField(request, "answers", List.of(item1));

        InterviewQuestion question1 = InterviewQuestion.builder().id(1L).build();

        Interview mockInterview = Interview.builder()
                .id(1L)
                .questions(List.of(question1))
                .build();

        given(interviewRepository.findById(request.getInterviewId())).willReturn(Optional.of(mockInterview));

        // when
        interviewService.submitAnswers(request);

        // then
        ArgumentCaptor<List<InterviewAnswer>> captor = ArgumentCaptor.forClass(List.class);
        verify(interviewAnswerRepository).saveAll(captor.capture());

        List<InterviewAnswer> savedAnswers = captor.getValue();
        assertThat(savedAnswers).hasSize(1);
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
        String fakeFeedbackJson = "{\"summary\":\"Great job!\"}";
        InterviewResult mockResult = InterviewResult.builder()
                .id(1L)
                .interview(mockInterview)
                .totalScore(85)
                .grade(Grade.B)
                .feedback(fakeFeedbackJson)
                .build();

        given(interviewResultRepository.findByInterviewId(interviewId)).willReturn(Optional.of(mockResult));

        // when
        InterviewResultResponse response = interviewService.getInterviewResult(interviewId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInterviewId()).isEqualTo(interviewId);
        assertThat(response.getResultId()).isEqualTo(1L);
        assertThat(response.getFeedback()).isEqualTo(fakeFeedbackJson);
    }
}
