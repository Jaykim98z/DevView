package com.allinone.DevView.mypage.service;

import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.mypage.dto.*;
import com.allinone.DevView.mypage.mapper.MypageMapper;
import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.mypage.repository.ScrapRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MypageService {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final ScrapRepository scrapRepository;
    private final MypageMapper mypageMapper;

    public MypageResponseDto getMypageData(Long userId) {
        User user = getUser(userId);

        List<InterviewDto> interviewDtos = interviewRepository.findAllByUserId(userId).stream()
                .sorted(Comparator.comparing(i -> i.getInterviewDate(), Comparator.reverseOrder()))
                .map(mypageMapper::toInterviewDto)
                .collect(Collectors.toList());

        List<ScrapDto> scrapDtos = scrapRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mypageMapper::toScrapDto)
                .collect(Collectors.toList());

        double avgScore = interviewDtos.stream()
                .mapToInt(InterviewDto::getScore)
                .average()
                .orElse(0.0);

        String grade = calculateGrade(avgScore);

        return MypageResponseDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .job(user.getJob())
                .careerLevel(user.getCareerLevel())
                .profileImageUrl(user.getProfileImageUrl())
                .memberId(user.getUserId())
                .joinedAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .interviews(interviewDtos)
                .scraps(scrapDtos)
                .totalInterviews(interviewDtos.size())
                .avgScore((int) avgScore)
                .grade(grade)
                .build();
    }

    public ScoreGraphDto getScoreGraphData(Long userId) {
        List<com.allinone.DevView.interview.entity.Interview> interviews = interviewRepository.findAllByUserId(userId).stream()
                .sorted(Comparator.comparing(i -> i.getInterviewDate()))
                .toList();

        List<String> labels = interviews.stream()
                .map(i -> i.getInterviewDate().format(DateTimeFormatter.ofPattern("MM월 dd일")))
                .collect(Collectors.toList());

        List<Integer> scores = interviews.stream()
                .map(i -> i.getScore())
                .collect(Collectors.toList());

        return new ScoreGraphDto(labels, scores);
    }

    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getUser(userId);
        user.setName(request.getName());
        user.setJob(request.getJob());
        user.setCareerLevel(request.getCareerLevel());
        userRepository.save(user);
    }

    public UserProfileUpdateRequest getBasicUserInfo(Long userId) {
        User user = getUser(userId);
        UserProfileUpdateRequest dto = new UserProfileUpdateRequest();
        dto.setName(user.getName());
        dto.setJob(user.getJob());
        dto.setCareerLevel(user.getCareerLevel());
        return dto;
    }

    private String calculateGrade(double avgScore) {
        if (avgScore >= 90) return "A";
        if (avgScore >= 80) return "B";
        if (avgScore >= 70) return "C";
        return "F";
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
