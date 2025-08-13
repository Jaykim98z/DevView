package com.allinone.DevView.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentsDto {

    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    public CommentsDto(Long commentId, Long postId, Long userId, String content, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static CommentsDto fromEntity(com.allinone.DevView.community.entity.Comments c) {
        return new CommentsDto(
                c.getId(),
                c.getPostId(),
                c.getUserId(),
                c.getContent(),
                c.getCreatedAt()
        );
    }

    @Data
    @NoArgsConstructor
    public static class CreateReq {
        private Long parentId;

        @NotBlank
        @Size(max = 2000)
        private String content;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateReq {
        @NotBlank
        @Size(max = 2000)
        private String content;
    }

    @Data
    @NoArgsConstructor
    public static class Res {
        private Long id;
        private Long userId;
        private String writerName;
        private String content;
        private LocalDateTime createdAt;

        public Res(Long id, Long userId, String writerName, String content, LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.writerName = writerName;
            this.content = content;
            this.createdAt = createdAt;
        }

        public static Res fromEntity(com.allinone.DevView.community.entity.Comments c, boolean mine) {
            return new Res(
                    c.getId(),
                    c.getUserId(),
                    c.getWriterName(),
                    c.getContent(),
                    c.getCreatedAt()
            );
        }
    }
}