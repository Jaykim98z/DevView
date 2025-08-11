package com.allinone.DevView.mypage.mapper;

import com.allinone.DevView.interview.entity.Interview;
import com.allinone.DevView.interview.entity.InterviewResult;
import com.allinone.DevView.mypage.dto.InterviewDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class MypageMapper {

    public InterviewDto toInterviewDto(Interview interview, InterviewResult result) {
        return InterviewDto.builder()
                .interviewId(interview.getId())
                .interviewDate(interview.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .interviewType(interview.getInterviewType().name())
                .score(result != null ? result.getTotalScore() : 0)
                .feedback(result != null ? result.getFeedback() : "")
                .build();
    }

}
