package com.allinone.DevView.mypage.service;

import com.allinone.DevView.common.exception.UserNotFoundException;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.allinone.DevView.mypage.dto.CareerChartDto;
import com.allinone.DevView.mypage.dto.InterviewDto;
import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.ScoreGraphDto;
import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.entity.UserProfile;
import com.allinone.DevView.mypage.mapper.MypageMapper;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final UserRepository userRepository;
    private final MypageMapper mypageMapper;
    private final InterviewRepository interviewRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final ProfileImageService profileImageService;

    /** 마이페이지 메인 데이터 (면접 요약/목록 포함) */
    public MypageResponseDto getMypageData(Long userId) {
        User user = getUserOrThrow(userId);

        // ✅ 사용자별 인터뷰 결과만 조회
        List<InterviewResult> results = interviewResultRepository.findByUserId(userId);

        // 최신순: endedAt 우선, 없으면 createdAt
        results.sort(Comparator.comparing((InterviewResult r) -> {
            Interview i = r.getInterview();
            return i.getEndedAt() != null ? i.getEndedAt() : i.getCreatedAt();
        }).reversed());

        // 목록 DTO
        List<InterviewDto> interviews = results.stream()
                .map(InterviewDto::fromEntity)
                .toList();

        int totalInterviews = results.size();
        int avgScore = (int) Math.round(results.stream().mapToInt(InterviewResult::getTotalScore).average().orElse(0));
        String latestGrade = results.isEmpty() ? null : results.get(0).getGrade().name();

        return MypageResponseDto.from(user, totalInterviews, avgScore, latestGrade, interviews, Collections.emptyList());
    }

    /** 점수 그래프 데이터 (최근 8개, 과거→현재) */
    public ScoreGraphDto getScoreGraphData(Long userId) {
        // ✅ 사용자별 인터뷰 결과만 조회
        List<InterviewResult> results = interviewResultRepository.findByUserId(userId);

        // 과거→현재: endedAt 우선, 없으면 createdAt
        results.sort(Comparator.comparing((InterviewResult r) -> {
            Interview i = r.getInterview();
            return i.getEndedAt() != null ? i.getEndedAt() : i.getCreatedAt();
        }));

        // 최근 8개
        int size = results.size();
        int from = Math.max(0, size - 8);
        List<InterviewResult> last = results.subList(from, size);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM월 dd일");
        List<String> labels = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();

        for (InterviewResult r : last) {
            Interview i = r.getInterview();
            LocalDateTime when = (i.getEndedAt() != null ? i.getEndedAt() : i.getCreatedAt());
            labels.add(when.format(fmt));
            scores.add(r.getTotalScore());
        }

        return new ScoreGraphDto(labels, scores);
    }

    /** 직무 차트 데이터 (기존 유지) */
    public CareerChartDto getCareerChartData(Long userId) {
        Map<String, Long> jobCounts = interviewRepository.findAllByUserId(userId).stream()
                .collect(Collectors.groupingBy(Interview::getJobPosition, Collectors.counting()));
        List<String> labels = new ArrayList<>(jobCounts.keySet());
        List<Integer> data = jobCounts.values().stream().map(Long::intValue).toList();
        return new CareerChartDto(labels, data);
    }

    /** 기본 프로필 조회 (기존 유지) */
    public MypageResponseDto getBasicUserInfo(Long userId) {
        User user = getUserOrThrow(userId);
        return buildBasicResponse(user);
    }

    /** 프로필 정보/이미지 저장 (기존 유지) */
    @Transactional
    public MypageResponseDto updateProfile(Long userId, UserProfileUpdateRequest profileReq, MultipartFile profileImage) {
        User user = getUserOrThrow(userId);

        UserProfile userProfile = Optional.ofNullable(user.getUserProfile())
                .orElseGet(() -> {
                    UserProfile np = UserProfile.builder().user(user).build();
                    user.setUserProfile(np);
                    return np;
                });

        mypageMapper.applyProfileUpdates(user, userProfile, profileReq);

        if (profileImage != null && !profileImage.isEmpty()) {
            String savedUrl = profileImageService.uploadProfileImage(userId, profileImage);
            userProfile.setProfileImageUrl(savedUrl);
        }

        userRepository.save(user);
        return buildBasicResponse(user);
    }

    /** 프로필 이미지 삭제 (기존 유지) */
    @Transactional
    public MypageResponseDto deleteProfileImage(Long userId) {
        User user = getUserOrThrow(userId);
        UserProfile profile = user.getUserProfile();
        if (profile != null && profile.getProfileImageUrl() != null) {
            profileImageService.deleteProfileImage(userId, profile.getProfileImageUrl());
            profile.setProfileImageUrl(null);
        }
        userRepository.save(user);
        return buildBasicResponse(user);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private MypageResponseDto buildBasicResponse(User user) {
        return MypageResponseDto.from(user, 0, 0, null, Collections.emptyList(), Collections.emptyList());
    }
}
