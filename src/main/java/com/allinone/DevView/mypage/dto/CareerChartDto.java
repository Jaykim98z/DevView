package com.allinone.DevView.mypage.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.List;

@Getter
public class CareerChartDto {

    private final List<String> labels;
    private final List<Integer> data;

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
