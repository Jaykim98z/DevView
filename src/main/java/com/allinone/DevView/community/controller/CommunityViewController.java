package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommunityPostDetailDto;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityViewController {

    private final CommunityService communityService;

    @GetMapping
    public String getCommunityMain(Model model) {
        List<CommunityPostsDto> posts;
        try {
            posts = communityService.getAllPostDtos();
        } catch (Exception e) {
            log.error("Failed to load community posts", e);
            posts = Collections.emptyList(); // 뷰 안전
        }
        model.addAttribute("posts", posts);
        return "community/community";
    }

    @GetMapping("/posts/{id}/detail")
    public String getPostDetail(@PathVariable Long id, Model model) {
        CommunityPostDetailDto post = communityService.getPostDetailDto(id);
        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        model.addAttribute("post", post);
        return "community/post-detail";
    }
}
