package com.allinone.DevView.community.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CombinedPostRequest {

    public enum PostCategory {
        INTERVIEW_SHARE, FREE
    }

    private PostCategory category;

    @Valid
    private CreateInterviewSharePostRequest interviewShare;

    @Valid
    private CreatePostRequest freePost;

    @AssertTrue(message = "글 종류에 맞는 입력 데이터가 올바르지 않습니다: 인터뷰공유는 interviewShare가, 자유글은 freePost가 필요합니다.")
    public boolean isPayloadValid() {
        if (category == PostCategory.INTERVIEW_SHARE) {
            return interviewShare != null;
        }
        if (category == PostCategory.FREE) {
            return freePost != null;
        }

        boolean hasInterviewShare = (interviewShare != null);
        boolean hasFreePost = (freePost != null);
        return hasInterviewShare ^ hasFreePost;
    }

    public CreateInterviewSharePostRequest toInterviewShareRequest() { return interviewShare; }
    public CreatePostRequest toFreePostRequest() { return freePost; }

    public static CombinedPostRequest empty() {
        return CombinedPostRequest.builder().category(PostCategory.FREE).build();
    }

    public static CombinedPostRequest empty(PostCategory category) {
        return CombinedPostRequest.builder().category(category).build();
    }
}
