package com.allinone.DevView.mypage.service.mapper.MypageMapper;

import com.devview.interview.entity.Interview;
import com.devview.interview.repository.InterviewRepository;
import com.devview.mypage.dto.MypageResponseDto;
import com.devview.mypage.dto.UserProfileUpdateRequest;
import com.devview.mypage.entity.Scrap;
import com.devview.mypage.mapper.MypageMapper;
import com.devview.mypage.repository.ScrapRepository;
import com.devview.user.entity.User;
import com.devview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageMapper {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final ScrapRepository scrapRepository;
    private final MypageMapper mypageMapper;

    public MypageResponseDto getMypageData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Interview> interviews = interviewRepository.findByUserId(userId);
        List<Scrap> scraps = scrapRepository.findByUserId(userId);

        return MypageResponseDto.builder()
                .email(user.getEmail())
                .job(user.getJob())
                .careerLevel(user.getCareerLevel())
                .profileImageUrl(user.getProfileImageUrl())
                .userId(user.getUserId())
                .createdAt(user.getCreatedAt())
                .interviews(interviews.stream()
                        .map(mypageMapper::toInterviewDto)
                        .collect(Collectors.toList()))
                .scraps(scraps.stream()
                        .map(mypageMapper::toScrapDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setName(request.getName());
        user.setJob(request.getJob());
        user.setCareerLevel(request.getCareerLevel());

        userRepository.save(user);
    }
}
