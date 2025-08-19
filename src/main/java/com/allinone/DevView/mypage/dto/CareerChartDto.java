package com.allinone.DevView.mypage.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.List;

@Getter
public class CareerChartDto {

    private final List<String> labels; // 직무 이름 예: ["백엔드", "프론트", "AI"]
    private final List<Integer> data;  // 해당 직무 선호도/횟수 예: [40, 30, 30]

    public CareerChartDto(List<String> labels, List<Integer> data) {
        this.labels = labels;
        this.data = data;
    }

    public String getLabelsJson() {
        return toJson(labels);
    }

    public String getDataJson() {
        return toJson(data);
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}