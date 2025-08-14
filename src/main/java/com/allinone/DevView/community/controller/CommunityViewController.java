package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CombinedPostRequest;
import com.allinone.DevView.community.dto.CommunityPostDetailDto;
import com.allinone.DevView.community.dto.CommunityPostsDto;
import com.allinone.DevView.community.service.CommunityService;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
            posts = Collections.emptyList();
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
    public String newPostForm(Model model, Principal principal){
        if (principal == null) {
            return "redirect:/user/login?redirect=/community/posts/new";
        }
        model.addAttribute("form", CombinedPostRequest.empty());
        return "community/post-new";
    }

    @PostMapping("/posts/interview")
    @Transactional
    public String createCombinedByForm(
            @Valid @ModelAttribute("form") CombinedPostRequest form,
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
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));

        Long userId = user.getUserId();

        Long interviewPostId = communityService.createInterviewSharePost(
                form.getInterviewShare(), userId
        );

        communityService.createPost(
                form.getFreePost(), userId
        );

        return "redirect:/community/posts/" + interviewPostId + "/detail";
    }

    @GetMapping("/posts/interview/new")
    public String legacyInterviewNewRedirect() {
        return "redirect:/community/posts/new";
    }

    @PostMapping("/posts")
    public String legacyFreePostRedirect() {
        return "redirect:/community/posts/new";
    }
}
