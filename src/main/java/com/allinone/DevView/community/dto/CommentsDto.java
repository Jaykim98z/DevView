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
    private String writerName;
    private String content;
    private LocalDateTime createdAt;

    private boolean canEdit;
    private boolean canDelete;

    public CommentsDto(Long commentId, Long postId, Long userId,
                       String writerName, String content, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.writerName = writerName;
        this.content = content;
        this.createdAt = createdAt;
        this.canEdit = false;
        this.canDelete = false;
    }

    public static CommentsDto fromEntity(com.allinone.DevView.community.entity.Comments c) {
        return new CommentsDto(
                c.getId(),
                c.getPostId(),
                c.getUserId(),
                c.getWriterName(),
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
        private String username;
        private String writerName;
        private String content;
        private LocalDateTime createdAt;
        private boolean mine;

        public Res(Long id, Long userId, String username, String writerName, String content, LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.writerName = writerName;
            this.content = content;
            this.createdAt = createdAt;
        }

        public static Res of(com.allinone.DevView.community.entity.Comments c, String username) {
            String display = (username != null && !username.isBlank()) ? username : c.getWriterName();
            return new Res(
                    c.getId(),
                    c.getUserId(),
                    username,
                    display,
                    c.getContent(),
                    c.getCreatedAt()
            );
        }

        public static Res of(com.allinone.DevView.community.entity.Comments c, String username, Long viewerId, boolean isAdmin) {
            Res r = of(c, username);
            r.setMine(isAdmin || (viewerId != null && viewerId.equals(c.getUserId())));
            return r;
        }
    }
}
