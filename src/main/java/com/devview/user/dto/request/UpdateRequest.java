package com.devview.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequest {
    private String name;
    private String job;
    private String careerLevel;
}
