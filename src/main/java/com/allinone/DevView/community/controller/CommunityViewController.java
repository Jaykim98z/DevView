package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommunityPostDetailDto;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.service.CommunityService;
import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityViewController {

    private final CommunityService communityService;
    private final UserRepository userRepository;

    @GetMapping
    public String getCommunityMain(Model model) {
        try {
            List<CommunityPostsDto> posts = communityService.getAllPostDtos();
            model.addAttribute("posts", posts);
        } catch (Exception e) {
            e.printStackTrace(); // 로그에 예외 출력
        }
        return "community/community";
    }

    @GetMapping("/posts/{id}/detail")
    public String getPostDetail(@PathVariable Long id, Model model) {
        CommunityPostDetailDto post = communityService.getPostDetailDto(id);
        model.addAttribute("post", post);
        return "community/post-detail";
    }

    @PostConstruct
    public void insertMockPost() {
        if (communityService.getAllPostDtos().size() == 0) {
            var user = userRepository.findById(1L).orElse(null);
            if (user == null) return;

            CommunityPosts post = new CommunityPosts();
            post.setUser(user);
            post.setTitle("Spring 면접 후기");
            post.setContent("MSA 구조 설계, API Gateway, Eureka 질문 나옴");
            post.setGrade(Grade.B);
            post.setScore(88);
            post.setViewCount(123);
            post.setLikeCount(10);
            post.setScrapCount(5);
            post.setInterviewType("PRACTICE");
            post.setCreatedAt(LocalDateTime.now());

            communityService.createPost(post);
        }
    }
}
