package com.devview.mypage.mapper;

import com.devview.mypage.dto.InterviewDto;
import com.devview.mypage.dto.ScrapDto;
import com.devview.mypage.entity.Interview;
import com.devview.mypage.entity.Scrap;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class MypageMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public InterviewDto toInterviewDto(Interview interview) {
        int score = interview.getScore();
        String grade = calculateGrade(score);

        return InterviewDto.builder()
                .id(interview.getId())
                .title(interview.getTitle())
                .date(interview.getInterviewDate().format(DATE_FORMATTER))
                .score(score)
                .grade(grade)
                .build();
    }

    public ScrapDto toScrapDto(Scrap scrap) {
        return ScrapDto.builder()
                .title(scrap.getTitle())
                .link(scrap.getLink())
                .likes(scrap.getLikeCount())
                .comments(scrap.getCommentCount())
                .build();
    }

    private String calculateGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "F";
    }
}
