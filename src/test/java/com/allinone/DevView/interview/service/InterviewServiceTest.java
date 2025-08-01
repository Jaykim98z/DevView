package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.response.InterviewResponse;
import com.allinone.DevView.interview.dto.response.QuestionResponse;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewQuestion;
import com.allinone.DevView.interview.entity.InterviewType;
import com.allinone.DevView.interview.repository.InterviewQuestionRepository;
import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InterviewService interviewService;

    @Mock
    private ExternalAiApiService externalAiApiService;

    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;

    @Test
    @DisplayName("면접 시작 - 성공")
    void startInterview_success() {
        // given
        // 1. 실제 요청(Request) 객체 생성
        StartInterviewRequest request = new StartInterviewRequest();
        ReflectionTestUtils.setField(request, "userId", 1L);
        ReflectionTestUtils.setField(request, "interviewType", InterviewType.PRACTICE);
        ReflectionTestUtils.setField(request, "jobPosition", "Backend Developer");
        ReflectionTestUtils.setField(request, "careerLevel", "Junior");

        // 2. 테스트용 User, Interview 객체를 빌더로 생성
        User user = User.builder().username("testuser").build();
        Interview savedInterview = Interview.builder()
                .user(user)
                .interviewType(request.getInterviewType())
                .jobPosition(request.getJobPosition())
                .careerLevel(request.getCareerLevel())
                .build();
        ReflectionTestUtils.setField(savedInterview, "id", 10L); // 저장 후 ID가 부여된 상황을 가정

        // 3. Mock 객체들의 행동 정의
        given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));
        given(interviewRepository.save(any(Interview.class))).willReturn(savedInterview);

        // when
        InterviewResponse response = interviewService.startInterview(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getInterviewId()).isEqualTo(10L);
        assertThat(response.getInterviewType()).isEqualTo(InterviewType.PRACTICE);
        assertThat(response.getJobPosition()).isEqualTo("Backend Developer");
    }

    @Test
    @DisplayName("질문 요청 - 성공")
    void askQuestion_success() {
        // given
        Long interviewId = 1L;
        Interview mockInterview = Interview.builder()
                .jobPosition("Backend Developer")
                .careerLevel("Junior")
                .build();
        String fakeQuestionText = "What is SOLID?";

        // Use the builder here instead of new()
        InterviewQuestion fakeSavedQuestion = InterviewQuestion.builder()
                .id(100L)
                .text(fakeQuestionText)
                .interview(mockInterview)
                .build();

        given(interviewRepository.findById(interviewId)).willReturn(Optional.of(mockInterview));
        given(externalAiApiService.getQuestionFromAi(any(String.class), any(String.class))).willReturn(fakeQuestionText);
        given(interviewQuestionRepository.save(any(InterviewQuestion.class))).willReturn(fakeSavedQuestion);

        // when
        QuestionResponse response = interviewService.askQuestion(interviewId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getQuestionId()).isEqualTo(100L);
        assertThat(response.getText()).isEqualTo(fakeQuestionText);
    }
}
