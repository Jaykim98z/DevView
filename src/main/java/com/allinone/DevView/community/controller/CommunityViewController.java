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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String newPostForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/user/login?redirect=/community/posts/new";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", CombinedPostRequest.empty());
        }
        return "community/post-write";
    }

    @GetMapping("/posts/interview")
    public String interviewPostForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/user/login?redirect=/community/posts/interview";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", CombinedPostRequest.empty());
        }
        return "community/post-write";
    }

    @PostMapping("/posts/interview")
    @Transactional
    public String createCombinedByForm(@Valid @ModelAttribute("form") CombinedPostRequest form,
                                       BindingResult bindingResult,
                                       Principal principal,
                                       RedirectAttributes rttr,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);
            return "community/post-write";
        }
        if (principal == null) {
            return "redirect:/user/login?redirect=/community/posts/interview";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다."));
        Long userId = user.getUserId();

        // ★ 결합 저장 메서드: 하나의 게시글(postId) 생성
        Long postId = communityService.createInterviewSharePost(
                form.getInterviewShare(),
                form.getFreePost(),
                userId
        );

        rttr.addFlashAttribute("message", "등록 완료되었습니다.");
        return "redirect:/community?created=" + postId;
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
