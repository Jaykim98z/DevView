package com.allinone.DevView.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ScrapDto {
    private final Long postId;

    private final String title;
    private final String link;
    private final int likes;
    private final int comments;

    private final String writerName;
    private final String preview;

    public ScrapDto(String title, String link, Number likes, Number comments,
                    String writerName, String preview) {
        this(null, title, link, likes, comments, writerName, preview);
    }

    public ScrapDto(Long postId, String title, String link, Number likes, Number comments,
                    String writerName, String preview) {
        this.postId = postId;
        this.title = title;
        this.link = normalizeLink(link, postId);
        this.likes = (likes == null) ? 0 : likes.intValue();
        this.comments = (comments == null) ? 0 : comments.intValue();
        this.writerName = writerName;
        this.preview = preview;
    }

    private static String normalizeLink(String raw, Long postId) {
        if (raw != null && !raw.trim().isEmpty()) {
            return raw;
        }
        return (postId != null) ? "/community/posts/" + postId + "/detail" : "#";
    }
}
