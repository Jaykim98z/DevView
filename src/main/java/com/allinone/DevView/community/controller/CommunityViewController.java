package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommunityPostDetailDto;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.dto.CreatePostRequest;
import com.allinone.DevView.community.service.CommunityService;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityViewController {

    private final CommunityService communityService;
    private final UserRepository userRepository;

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

    @GetMapping("/posts/new")
    public String newPostForm(Model model){
        // ✅ CreatePostRequest는 기본 생성자 없음 → 9개 인자 생성자 사용
        model.addAttribute("form",
                new CreatePostRequest(
                        "", "",              // title, content
                        "PRACTICE",         // interviewType (문자열로 두고 화면에서 변경)
                        "C",                // grade
                        null, null,         // techTag, level
                        null,               // category
                        null,               // type
                        null                // score
                )
        );
        return "community/post-new";
    }

    @PostMapping("/posts")
    public String createPost(
            @Valid @ModelAttribute("form") CreatePostRequest form,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            return "community/post-new";
        }

        if (principal == null) {
            return "redirect:/user/login?redirect=/community/posts/new";
        }

        String email = principal.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        communityService.createPost(form, user.getUserId());

        return "redirect:/community";
    }
}
