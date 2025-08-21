package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.CommunityPosts;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CommunityPostSpecs {

    private static String norm(String s) {
        return (s == null) ? null : s.trim().toLowerCase();
    }

    public static Specification<CommunityPosts> categoryEq(String category) {
        String c = norm(category);
        if (!StringUtils.hasText(c) || "전체".equals(c)) return null;
        return (root, q, cb) -> {
            var path = cb.lower(cb.coalesce(root.get("category"), ""));
            return cb.like(path, "%" + c + "%");
        };
    }

    public static Specification<CommunityPosts> levelEq(String level) {
        String l = norm(level);
        if (!StringUtils.hasText(l) || "전체".equals(l)) return null;
        return (root, q, cb) -> {
            var path = cb.lower(cb.coalesce(root.get("level"), ""));
            return cb.like(path, "%" + l + "%");
        };
    }
}
