package com.allinone.DevView.interview.service;

import com.allinone.DevView.common.exception.CustomException;
import com.allinone.DevView.common.exception.ErrorCode;
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
import com.allinone.DevView.ranking.service.RankingService;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
    private final ObjectMapper objectMapper;
    // 🆕 랭킹 서비스 연동 (순환 의존성 해결을 위해 @Lazy 사용)
    @Lazy
    @Autowired
    private RankingService rankingService;


    @Transactional
    public InterviewResponse startInterview(StartInterviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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

    public List<QuestionResponse> askAndSaveQuestions(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));

        List<String> questionTexts = gemini.getQuestionFromAi(
                interview.getJobPosition(),
                interview.getCareerLevel()
        );

        List<InterviewQuestion> newQuestions = questionTexts.stream()
                .map(text -> InterviewQuestion.builder()
                        .interview(interview)
                        .text(text)
                        .category(interview.getJobPosition())
                        .build())
                .collect(Collectors.toList());

        List<InterviewQuestion> savedQuestions = interviewQuestionRepository.saveAll(newQuestions);

        return savedQuestions.stream()
                .map(QuestionResponse::fromEntity)
                .collect(Collectors.toList());
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
    public void submitAnswers(SubmitAnswerRequest request) {
        List<Long> questionIds = request.getAnswers().stream()
                .map(SubmitAnswerRequest.AnswerItem::getQuestionId)
                .collect(Collectors.toList());

        Map<Long, InterviewQuestion> questionMap = interviewQuestionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(InterviewQuestion::getId, q -> q));

        List<InterviewAnswer> newAnswers = request.getAnswers().stream()
                .map(item -> {
                    InterviewQuestion question = questionMap.get(item.getQuestionId());
                    if (question == null) {
                        throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
                    }
                    return InterviewAnswer.builder()
                            .question(question)
                            .answerText(item.getAnswerText())
                            .build();
                })
                .collect(Collectors.toList());

        interviewAnswerRepository.saveAll(newAnswers);
    }

    @Transactional
    public InterviewResultResponse endInterview(Long interviewId) {
        Interview interview = interviewRepository.findByIdWithQuestions(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));

        // Fetch questions and answers from the database
        // NOTE: This is a simple fetch. In a real-world scenario with many Q&As,
        // you would use a more optimized query.
        List<InterviewQuestion> questions = interview.getQuestions();
        List<InterviewAnswer> answers = interviewAnswerRepository.findByQuestionIn(questions);

        // 2. Create a transcript for the AI to analyze.
        String transcript = createTranscript(questions, answers);

        log.info("Generated Transcript for AI Analysis:\n{}", transcript);

        // 3. Create the prompt for Gemini.
        String prompt = createAnalysisPrompt(interview, transcript);

        log.info("Generated Prompt for Gemini:\n{}", prompt);

        String aiResponseJson = gemini.generateContent(prompt);
        String cleanedJson = aiResponseJson.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim();

        try {
            GeminiAnalysisResponseDto analysis = objectMapper.readValue(cleanedJson, GeminiAnalysisResponseDto.class);
            String recommendations = getRecommendationsFromAlan(interview.getJobPosition());

            interview.endInterviewSession();

            InterviewResult result = InterviewResult.builder()
                    .interview(interview)
                    .totalScore(analysis.totalScore())
                    .grade(calculateGrade(analysis.totalScore()))
                    .feedback(analysis.feedback())
                    .recommendedResource(recommendations)
                    .build();

            InterviewResult savedResult = interviewResultRepository.save(result);

            // 면접 완료 후 랭킹 업데이트
            try {
                Long userId = interview.getUser().getUserId();
                rankingService.updateUserRanking(userId);
                log.info("면접 완료 후 랭킹 업데이트 성공: userId={}, interviewId={}, newScore={}",
                        userId, interviewId, analysis.totalScore());
            } catch (Exception e) {
                // 랭킹 업데이트 실패해도 면접 결과는 정상 반환 (독립적 처리)
                log.error("면접 완료 후 랭킹 업데이트 실패: interviewId={}, error={}",
                        interviewId, e.getMessage(), e);
            }

            return InterviewResultResponse.fromEntity(savedResult);
        } catch (Exception e) {
            log.error("Failed to parse JSON response from AI. Raw Response: {}", aiResponseJson, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public InterviewResultResponse getInterviewResult(Long interviewId) {
        InterviewResult result = interviewResultRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));

        return InterviewResultResponse.fromEntity(result);
    }

    private String createTranscript(List<InterviewQuestion> questions, List<InterviewAnswer> answers) {
        return questions.stream()
                .map(q -> {
                    String answerText = answers.stream()
                            .filter(a -> a.getQuestion().getId().equals(q.getId()))
                            .findFirst()
                            .map(InterviewAnswer::getAnswerText)
                            .orElse("No answer provided.");
                    return "Q: " + q.getText() + "\nA: " + answerText;
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String createAnalysisPrompt(Interview interview, String transcript) {
        return "As an expert interviewer, please evaluate the following interview transcript for a " +
                interview.getJobPosition() + " role. Provide a total score, constructive feedback, a summary, " +
                "and four category scores. Your response MUST be a single, valid JSON object with no extra text. " +
                "The JSON object must have these exact keys: 'totalScore' (0-100), 'feedback' (string), 'summary' (string), " +
                "'techScore' (0-100), 'problemScore' (0-100), 'commScore' (0-100), 'attitudeScore' (0-100).\n\n" +
                "Here is the transcript:\n" + transcript;
    }

    private String getRecommendationsFromAlan(String jobPosition) {
        try {
            if (alan instanceof AlanApiService) {
                return ((AlanApiService) alan).getRecommendations(jobPosition);
            }
        } catch (Exception e) {
            log.error("Failed to get recommendations from Alan API", e);
        }
        return "No recommendations available.";
    }

    private Grade calculateGrade(int score) {
        if (score >= 90) return Grade.A;
        if (score >= 80) return Grade.B;
        if (score >= 70) return Grade.C;
        if (score >= 60) return Grade.D;
        return Grade.F;
    }
}
