package com.allinone.DevView.community.service;

import com.allinone.DevView.common.exception.CustomException;
import com.allinone.DevView.common.exception.ErrorCode;
import com.allinone.DevView.interview.dto.response.InterviewResultResponse;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.interview.repository.InterviewResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewResultQueryService {

    private final InterviewResultRepository repo;

    /** 로그인 유저의 최신 결과 */
    @Transactional(readOnly = true)
    public InterviewResultResponse findLatestByUserId(Long userId) {
        InterviewResult r = repo.findTopByInterview_User_UserIdOrderByInterview_CreatedAtDesc(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));
        return InterviewResultResponse.fromEntity(r);
    }

    /** 특정 resultId를 해당 유저 권한으로 조회 */
    @Transactional(readOnly = true)
    public InterviewResultResponse findByIdForUser(Long resultId, Long userId) {
        InterviewResult r = repo.findByIdAndInterview_User_UserId(resultId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));
        return InterviewResultResponse.fromEntity(r);
    }
}
