package com.allinone.DevView.community.controller;

import com.allinone.DevView.community.dto.CommentsDto;
import com.allinone.DevView.community.service.CommentsService;
import com.allinone.DevView.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/posts/{postId}/comments")
public class CommentsController {

    private final CommentsService commentsService;

    private Long getUserId(CustomUserDetails auth) { return (auth != null) ? auth.getUserId() : null; }
    private String getWriterName(CustomUserDetails auth) { return (auth != null) ? auth.getUsername() : "익명"; }

    @GetMapping(produces = "application/json")
    public Page<CommentsDto.Res> list(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails auth
    ) {
        int pageSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long me = getUserId(auth);
        return commentsService.list(postId, me, pageable);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentsDto.Res create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentsDto.CreateReq req,
            @AuthenticationPrincipal CustomUserDetails auth
    ) {
        Long me = getUserId(auth);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        String writerName = getWriterName(auth);
        return commentsService.create(postId, me, writerName, req);
    }

    @PutMapping(value = "/{commentId}", consumes = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentsDto.UpdateReq req,
            @AuthenticationPrincipal CustomUserDetails auth
    ) {
        Long me = getUserId(auth);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        try {
            commentsService.update(commentId, me, req);
        } catch (SecurityException se) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, se.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails auth
    ) {
        Long me = getUserId(auth);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        try {
            commentsService.delete(commentId, me);
        } catch (SecurityException se) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, se.getMessage());
        }
    }
}
