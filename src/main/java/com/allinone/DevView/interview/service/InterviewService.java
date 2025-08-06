package com.allinone.DevView.interview.service;

import com.allinone.DevView.interview.dto.request.StartInterviewRequest;
import com.allinone.DevView.interview.dto.request.SubmitAnswerRequest;
import com.allinone.DevView.interview.dto.response.AnswerResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final ExternalAiApiService gemini;
    private final ExternalAiApiService alan;
    private final InterviewResultRepository interviewResultRepository;

    @Transactional
    public InterviewResponse startInterview(StartInterviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // DTO를 Entity로 변환하는 로직이 필요합니다.
        // 여기서는 설명을 위해 간단히 생성합니다.
        Interview interview = Interview.builder()
                .user(user)
                .interviewType(request.getInterviewType())
                .jobPosition(request.getJobPosition())
                .careerLevel(request.getCareerLevel())
                .build();

        Interview savedInterview = interviewRepository.save(interview);

        return InterviewResponse.fromEntity(savedInterview);
    }

    public QuestionResponse askAndSaveQuestion(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        String questionText = gemini.getQuestionFromAi(
                interview.getJobPosition(),
                interview.getCareerLevel()
        );

        return saveQuestion(interview, questionText);
    }

    @Transactional
    public QuestionResponse saveQuestion(Interview interview, String questionText) {
        InterviewQuestion newQuestion = InterviewQuestion.builder()
                .interview(interview)
                .text(questionText)
                .category(interview.getJobPosition())
                .build();

        InterviewQuestion savedQuestion = interviewQuestionRepository.save(newQuestion);

        return QuestionResponse.fromEntity(savedQuestion);
    }

    @Transactional
    public AnswerResponse submitAnswer(SubmitAnswerRequest request) {
        InterviewQuestion question = interviewQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        InterviewAnswer newAnswer = InterviewAnswer.builder()
                .question(question)
                .answerText(request.getAnswerText())
                .build();

        InterviewAnswer savedAnswer = interviewAnswerRepository.save(newAnswer);

        return AnswerResponse.fromEntity(savedAnswer);
    }

    @Transactional
    public InterviewResultResponse endInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        interview.endInterviewSession();

        // 임시 결과 데이터를 생성합니다. (TODO: 추후 AI 분석 로직으로 대체)
        int score = 85;
        Grade grade = Grade.B;
        String feedback = "전반적으로 좋은 답변이었지만, 몇 가지 부분에서 보충이 필요해 보입니다.";

        InterviewResult result = InterviewResult.builder()
                .interview(interview)
                .totalScore(score)
                .grade(grade)
                .feedback(feedback)
                .build();

        InterviewResult savedResult = interviewResultRepository.save(result);

        return InterviewResultResponse.fromEntity(savedResult);
    }
}
