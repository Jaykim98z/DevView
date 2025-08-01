package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.request.SubmitAnswerRequest;
import com.allinone.DevView.interview.dto.response.AnswerResponse;
import com.allinone.DevView.interview.dto.response.InterviewResponse;
import com.allinone.DevView.interview.dto.response.QuestionResponse;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewAnswer;
import com.allinone.DevView.interview.entity.InterviewQuestion;
import com.allinone.DevView.interview.entity.InterviewType;
import com.allinone.DevView.interview.repository.InterviewAnswerRepository;
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

    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;

    @Mock
    private InterviewAnswerRepository interviewAnswerRepository;

    @InjectMocks
    private InterviewService interviewService;

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
    @DisplayName("면접 질문 저장 - 성공")
    void saveQuestion_success() {
        // given
        // 테스트에 필요한 객체들을 준비합니다.
        User user = User.builder().id(1L).build();
        Interview mockInterview = Interview.builder()
                .id(1L)
                .user(user)
                .jobPosition("Backend")
                .build();
        String questionText = "What is SOLID?";
        InterviewQuestion savedQuestion = InterviewQuestion.builder()
                .id(100L)
                .interview(mockInterview)
                .text(questionText)
                .build();

        // interviewQuestionRepository.save가 호출되면, 준비된 객체를 반환하도록 설정
        given(interviewQuestionRepository.save(any(InterviewQuestion.class))).willReturn(savedQuestion);

        // when
        // 이제 `saveQuestion` 메서드를 직접 호출하여 테스트합니다.
        QuestionResponse response = interviewService.saveQuestion(mockInterview, questionText);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getQuestionId()).isEqualTo(100L);
        assertThat(response.getText()).isEqualTo(questionText);
    }

    @Test
    @DisplayName("답변 제출 - 성공")
    void submitAnswer_success() {
        // given
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        ReflectionTestUtils.setField(request, "questionId", 100L);
        ReflectionTestUtils.setField(request, "answerText", "It is a set of principles for object-oriented design.");

        InterviewQuestion mockQuestion = InterviewQuestion.builder().id(100L).build();

        // Use the builder to create the test object
        InterviewAnswer savedAnswer = InterviewAnswer.builder()
                .id(1L)
                .question(mockQuestion)
                .answerText(request.getAnswerText())
                .build();

        given(interviewQuestionRepository.findById(request.getQuestionId())).willReturn(Optional.of(mockQuestion));
        given(interviewAnswerRepository.save(any(InterviewAnswer.class))).willReturn(savedAnswer);

        // when
        AnswerResponse response = interviewService.submitAnswer(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAnswerId()).isEqualTo(1L);
        assertThat(response.getQuestionId()).isEqualTo(100L);
    }
}
