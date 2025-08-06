package com.allinone.DevView.mypage.mapper;

import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.mypage.dto.InterviewDto;
import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.ScrapDto;
import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.entity.Scrap;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.interview.repository.InterviewRepository;
import com.allinone.DevView.mypage.repository.ScrapRepository;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MypageMapper {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final ScrapRepository scrapRepository;

    // MypageResponseDto를 생성하는 메서드
    public MypageResponseDto getMypageData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 'findAllByUserId'로 변경
        List<Interview> interviews = interviewRepository.findAllByUserId(userId);
        List<Scrap> scraps = scrapRepository.findByUserId(userId);

        // 날짜 포맷 설정 (yyyy-MM-dd HH:mm:ss)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = user.getCreatedAt().format(formatter);

        return MypageResponseDto.builder()
                .email(user.getEmail())
                .job(user.getJob())
                .careerLevel(user.getCareerLevel())
                .profileImageUrl(user.getProfileImageUrl())
                .memberId(user.getUserId())
                .joinedAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .interviews(interviews.stream()
                        .map(this::toInterviewDto)
                        .collect(Collectors.toList()))
                .scraps(scraps.stream()
                        .map(this::toScrapDto)
                        .collect(Collectors.toList()))
                .build();
    }

    // 프로필 업데이트 메서드
    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setName(request.getName());
        user.setJob(request.getJob());
        user.setCareerLevel(request.getCareerLevel());

        userRepository.save(user);
    }

    // Interview 엔티티를 InterviewDto로 변환
    public InterviewDto toInterviewDto(Interview interview) {
        return InterviewDto.builder()
                .interviewId(interview.getId())
                .interviewDate(interview.getDate())   // 날짜 변환
                .interviewType(interview.getInterviewType())  // 면접 유형
                .score(interview.getScore())  // 점수
                .feedback(interview.getFeedback())  // 피드백
                .build();
    }

    // Scrap 엔티티를 ScrapDto로 변환
    public ScrapDto toScrapDto(Scrap scrap) {
        return ScrapDto.builder()
                .scrapId(scrap.getId())
                .scrapTitle(scrap.getTitle())
                .build();
    }
}
