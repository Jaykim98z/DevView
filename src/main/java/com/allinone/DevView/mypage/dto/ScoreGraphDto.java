package com.allinone.DevView.mypage.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.List;

@Getter
public class ScoreGraphDto {

    private final List<String> labels;
    private final List<Integer> scores;

    public ScoreGraphDto(List<String> labels, List<Integer> scores) {
        this.labels = labels;
        this.scores = scores;
    }

    /*** Thymeleaf에서 사용될 JSON 문자열로 반환*/
    public String getLabelsJson() {
        return toJson(labels);
    }

    public String getScoresJson() {
        return toJson(scores);
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
