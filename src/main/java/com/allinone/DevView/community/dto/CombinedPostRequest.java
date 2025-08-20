package com.allinone.DevView.community.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CombinedPostRequest {

    @Valid
    @NotNull
    private CreateInterviewSharePostRequest interviewShare = new CreateInterviewSharePostRequest();

    @Valid
    @NotNull
    private CreatePostRequest freePost = new CreatePostRequest();

    public static CombinedPostRequest empty() {
        CombinedPostRequest form = new CombinedPostRequest();

        form.getFreePost().setInterviewType("PRACTICE");
        form.getFreePost().setGrade("C");
        return form;
    }

    public CreateInterviewSharePostRequest toInterviewShareRequest() {
        return interviewShare;
    }

    public CreatePostRequest toFreePostRequest() {
        return freePost;
    }
}
