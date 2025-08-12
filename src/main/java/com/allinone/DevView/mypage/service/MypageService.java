package com.allinone.DevView.mypage.service;

import com.allinone.DevView.common.exception.UserNotFoundException;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.allinone.DevView.mypage.dto.CareerChartDto;
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

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final UserRepository userRepository;
    private final MypageMapper mypageMapper;

    private final InterviewRepository interviewRepository;
    private final InterviewResultRepository interviewResultRepository;

    // 이미지 저장/삭제 전담 서비스
    private final ProfileImageService profileImageService;

    /** 마이페이지 메인 데이터 */
    public MypageResponseDto getMypageData(Long userId) {
        User user = getUserOrThrow(userId);
        return buildBasicResponse(user);
    }

    /** 점수 그래프 데이터 */
    public ScoreGraphDto getScoreGraphData(Long userId) {
        List<Interview> interviews = interviewRepository.findAllByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(Interview::getCreatedAt))
                .toList();

        Set<Long> interviewIds = interviews.stream()
                .map(Interview::getId)
                .collect(Collectors.toSet());

        Map<Long, InterviewResult> resultByInterviewId = interviewResultRepository.findAll().stream()
                .filter(r -> r.getInterview() != null && interviewIds.contains(r.getInterview().getId()))
                .collect(Collectors.toMap(r -> r.getInterview().getId(), Function.identity(), (a, b) -> a));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일");
        List<String> labels = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();

        for (Interview interview : interviews) {
            labels.add(interview.getCreatedAt().format(formatter));
            InterviewResult result = resultByInterviewId.get(interview.getId());
            scores.add(result != null ? result.getTotalScore() : 0);
        }

        return new ScoreGraphDto(labels, scores);
    }

    /** 직무 차트 데이터 */
    public CareerChartDto getCareerChartData(Long userId) {
        Map<String, Long> jobCounts = interviewRepository.findAllByUserId(userId).stream()
                .collect(Collectors.groupingBy(Interview::getJobPosition, Collectors.counting()));
        List<String> labels = new ArrayList<>(jobCounts.keySet());
        List<Integer> data = jobCounts.values().stream().map(Long::intValue).toList();
        return new CareerChartDto(labels, data);
    }

    /** 기본 프로필 조회 */
    public MypageResponseDto getBasicUserInfo(Long userId) {
        User user = getUserOrThrow(userId);
        return buildBasicResponse(user);
    }

    /** 프로필 정보/이미지 저장 (프론트 FormData와 1:1 매칭) */
    @Transactional
    public MypageResponseDto updateProfile(Long userId, UserProfileUpdateRequest profileReq, MultipartFile profileImage) {
        User user = getUserOrThrow(userId);

        // 프로필 없으면 생성 + 양방향 매핑
        UserProfile userProfile = Optional.ofNullable(user.getUserProfile())
                .orElseGet(() -> {
                    UserProfile np = UserProfile.builder().user(user).build();
                    user.setUserProfile(np);
                    return np;
                });

        // 텍스트 필드 반영
        mypageMapper.applyProfileUpdates(user, userProfile, profileReq);
        // 이미지 파일이 있으면 실제 저장 후 URL 반영
        if (profileImage != null && !profileImage.isEmpty()) {
            String savedUrl = profileImageService.uploadProfileImage(userId, profileImage);
            userProfile.setProfileImageUrl(savedUrl);
        }

        // 저장 (CascadeType.ALL → 프로필도 함께 저장)
        userRepository.save(user);

        // 반영된 데이터 반환
        return buildBasicResponse(user);
    }

    /** 프로필 이미지 삭제 */
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
