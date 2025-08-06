package com.allinone.DevView.community.entity;

import java.io.Serializable;
import java.util.Objects;

public class LikesId implements Serializable {
    private Long userId;
    private Long postId;

    // 기본 생성자
    public LikesId() {}

    // 각 필드에 대한 getter & setter 추가
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof LikesId)) return false;
        LikesId that = (LikesId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }
}
