package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CommentsDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.repository.CommentsRepository;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentsDto.Res create(Long postId, Long userId, String writerName, CommentsDto.CreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        Comments c = new Comments();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setWriterName(writerName != null ? writerName : user.getUsername());
        c.setParentId(req.getParentId());
        c.setContent(req.getContent());
        c.setDeleted(false);

        Comments saved = commentsRepository.save(c);

        return new CommentsDto.Res(
                saved.getId(),
                saved.getUserId(),
                user.getUsername(),
                saved.getWriterName(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<CommentsDto.Res> list(Long postId, Long meUserId, Pageable pageable) {
        Page<Comments> page = commentsRepository.findByPostIdAndDeletedFalse(postId, pageable);

        List<Comments> items = page.getContent();
        if (items.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, page.getTotalElements());
        }

        Set<Long> userIds = items.stream()
                .map(Comments::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getUsername));

        List<CommentsDto.Res> resList = items.stream()
                .map(c -> {
                    String username = usernameMap.getOrDefault(c.getUserId(), c.getWriterName());
                    return new CommentsDto.Res(
                            c.getId(),
                            c.getUserId(),
                            username,
                            username,
                            c.getContent(),
                            c.getCreatedAt()
                    );
                })
                .toList();

        return new PageImpl<>(resList, pageable, page.getTotalElements());
    }

    @Transactional
    public void update(Long commentId, Long meUserId, CommentsDto.UpdateReq req) {
        Comments c = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));

        if (!Objects.equals(c.getUserId(), meUserId)) {
            throw new SecurityException("본인 댓글만 수정할 수 있습니다.");
        }

        c.edit(req.getContent());
    }

    @Transactional
    public void delete(Long commentId, Long meUserId) {
        Comments c = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));

        if (!Objects.equals(c.getUserId(), meUserId)) {
            throw new SecurityException("본인 댓글만 삭제할 수 있습니다.");
        }

        c.softDelete();
    }
}
