package com.allinone.DevView.community.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CombinedPostRequest {

    @Valid
    @NotNull
    private CreateInterviewSharePostRequest interviewShare;

    @Valid
    @NotNull
    private CreatePostRequest freePost;

    public static CombinedPostRequest empty() {
        return CombinedPostRequest.builder()
                .interviewShare(new CreateInterviewSharePostRequest())
                .freePost(new CreatePostRequest(
                        "", "",
                        "PRACTICE",
                        "C",
                        null,
                        null,
                        null,
                        null,
                        null
                ))
                .build();
    }

    public CreateInterviewSharePostRequest toInterviewShareRequest() {
        return interviewShare;
    }

    public CreatePostRequest toFreePostRequest() {
        return freePost;
    }
}
