package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.CommentsDto;
import com.allinone.DevView.community.entity.Comments;
import com.allinone.DevView.community.repository.CommentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;

    @Transactional
    public Long create(Long postId, Long userId, String writerName, CommentsDto.CreateReq req) {
        Comments c = new Comments();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setWriterName(writerName);
        c.setParentId(req.getParentId());
        c.setContent(req.getContent());
        c.setDeleted(false);
        commentsRepository.save(c);
        return c.getId();
    }

    @Transactional(readOnly = true)
    public Page<CommentsDto.Res> list(Long postId, Long meUserId, Pageable pageable) {
        Page<Comments> page = commentsRepository
                .findByPostIdAndDeletedFalseOrderByCreatedAtDesc(postId, pageable);

        return page.map(c -> new CommentsDto.Res(
                c.getId(),
                c.getUserId(),
                c.getWriterName(),
                c.getContent(),
                c.getCreatedAt()
        ));
    }

    @Transactional
    public void update(Long commentId, Long meUserId, CommentsDto.UpdateReq req) {
        Comments c = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));
        if (!c.getUserId().equals(meUserId)) {
            throw new SecurityException("본인 댓글만 수정할 수 있습니다.");
        }
        c.edit(req.getContent());
    }

    @Transactional
    public void delete(Long commentId, Long meUserId) {
        Comments c = commentsRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. id=" + commentId));
        if (!c.getUserId().equals(meUserId)) {
            throw new SecurityException("본인 댓글만 삭제할 수 있습니다.");
        }
        c.softDelete();
    }
}
