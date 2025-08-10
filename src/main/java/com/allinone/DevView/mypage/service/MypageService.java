package com.allinone.DevView.mypage.service;

import com.allinone.DevView.community.entity.Scraps;
import com.allinone.DevView.community.repository.CommunityPostsRepository;
import com.allinone.DevView.community.repository.ScrapsRepository;
import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import com.allinone.DevView.mypage.dto.CareerChartDto;
import com.allinone.DevView.mypage.dto.InterviewDto;
import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.ScoreGraphDto;
import com.allinone.DevView.mypage.dto.ScrapDto;
import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.mapper.MypageMapper;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MypageService {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final ScrapsRepository scrapsRepository;
    private final CommunityPostsRepository communityPostsRepository;
    private final MypageMapper mypageMapper;

    public MypageResponseDto getMypageData(Long userId) {
        User user = getUser(userId);

        List<InterviewDto> interviewDtos = interviewRepository.findAllByUserId(userId).stream()
                .sorted(Comparator.comparing(Interview::getCreatedAt).reversed())
                .map(interview -> {
                    InterviewResult result = interviewResultRepository
                            .findByInterviewId(interview.getId())
                            .orElse(null);
                    return mypageMapper.toInterviewDto(interview, result);
                })
                .collect(Collectors.toList());

        List<ScrapDto> scrapDtos = scrapsRepository.findByUserId(userId).stream()
                .map(this::toScrapDto)
                .filter(Objects::nonNull)
                .limit(5)
                .collect(Collectors.toList());

        double avgScore = interviewDtos.stream()
                .mapToInt(InterviewDto::getScore)
                .average()
                .orElse(0.0);

        String grade = calculateGrade(avgScore);

        return MypageResponseDto.builder()
                .name(user.getUsername())
                .email(user.getEmail())
                .job("-")                      // User 엔티티에 없음 → 기본값
                .careerLevel("-")              // User 엔티티에 없음 → 기본값
                .profileImageUrl(null)         // User 엔티티에 없음 → 기본값
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
        List<Interview> interviews = interviewRepository.findAllByUserId(userId).stream()
                .sorted(Comparator.comparing(Interview::getCreatedAt))
                .toList();

        List<String> labels = interviews.stream()
                .map(i -> i.getCreatedAt().format(DateTimeFormatter.ofPattern("MM월 dd일")))
                .collect(Collectors.toList());

        List<Integer> scores = interviews.stream()
                .map(i -> {
                    InterviewResult result = interviewResultRepository
                            .findByInterviewId(i.getId())
                            .orElse(null);
                    return result != null ? result.getTotalScore() : 0;
                })
                .collect(Collectors.toList());

        return new ScoreGraphDto(labels, scores);
    }

    public CareerChartDto getCareerChartData(Long userId) {
        // TODO: 커뮤니티/유저 도메인에서 실제 데이터 받아오기
        List<String> labels = List.of("백엔드", "프론트엔드", "AI", "데이터");
        List<Integer> data = List.of(40, 30, 20, 10); // mock 데이터

        return new CareerChartDto(labels, data);
    }

    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        // User 엔티티에는 name, job, careerLevel 없음 → 업데이트 생략 or log만
        User user = getUser(userId);
        System.out.println("업데이트 요청: name=" + request.getName() +
                ", job=" + request.getJob() +
                ", level=" + request.getCareerLevel());
    }

    public UserProfileUpdateRequest getBasicUserInfo(Long userId) {
        User user = getUser(userId);
        UserProfileUpdateRequest dto = new UserProfileUpdateRequest();
        dto.setName(user.getUsername());
        dto.setJob("-");
        dto.setCareerLevel("-");
        return dto;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));
    }

    private String calculateGrade(double avgScore) {
        if (avgScore >= 90) return "A";
        if (avgScore >= 80) return "B";
        if (avgScore >= 70) return "C";
        return "F";
    }

    private ScrapDto toScrapDto(Scraps scrap) {
        return communityPostsRepository.findById(scrap.getPostId())
                .map(post -> ScrapDto.builder()
                        .title(post.getTitle())
                        .link("/community/posts/" + post.getPostId())
                        .likes(post.getLikeCount())
                        .comments(0) // 댓글 수 없음 → 기본값 0
                        .build())
                .orElse(null);
    }
}
