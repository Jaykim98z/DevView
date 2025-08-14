package com.allinone.DevView.user.repository;

import com.allinone.DevView.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 데이터 접근 계층
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 찾기 (로그인 시 사용)
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 확인 (회원가입 시 사용)
     */
    boolean existsByEmail(String email);

    /**
     * 사용자명 중복 확인 (회원가입 시 사용)
     */
    boolean existsByUsername(String username);

    /**
     * OAuth2 사용자 찾기 (구글 로그인 시 사용)
     */
    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.providerId = :providerId")
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider,
                                               @Param("providerId") String providerId);

    /**
     * 로컬 사용자만 찾기
     */
    @Query("SELECT u FROM User u WHERE u.provider = 'LOCAL' OR u.provider IS NULL")
    Optional<User> findLocalUserByEmail(@Param("email") String email);

    /**
     * 회원탈퇴 시 관련 데이터 삭제
     * 외래키 제약 조건을 고려하여 순서대로 삭제
     */
    @Modifying
    @Transactional
    @Query(value = """
        -- 1. 면접 관련 데이터 삭제 (답변 -> 질문 -> 결과 -> 면접)
        DELETE FROM interview_answers WHERE question_id IN (
            SELECT question_id FROM interview_questions WHERE interview_id IN (
                SELECT interview_id FROM interviews WHERE user_id = :userId
            )
        );
        DELETE FROM interview_questions WHERE interview_id IN (
            SELECT interview_id FROM interviews WHERE user_id = :userId
        );
        DELETE FROM interview_results WHERE interview_id IN (
            SELECT interview_id FROM interviews WHERE user_id = :userId
        );
        DELETE FROM interviews WHERE user_id = :userId;
        
        -- 2. 커뮤니티 관련 데이터 삭제 (자식부터 부모 순서로)
        -- 2-1. 먼저 스크랩 삭제 (사용자가 스크랩한 것들)
        DELETE FROM scraps WHERE user_id = :userId;
        
        -- 2-2. 사용자 게시글에 달린 스크랩들 삭제
        DELETE FROM scraps WHERE post_id IN (
            SELECT id FROM community_posts WHERE user_id = :userId
        );
        
        -- 2-3. 좋아요 삭제 (사용자가 누른 것들)
        DELETE FROM likes WHERE user_id = :userId;
        
        -- 2-4. 사용자 게시글에 달린 좋아요들 삭제
        DELETE FROM likes WHERE post_id IN (
            SELECT id FROM community_posts WHERE user_id = :userId
        );
        
        -- 2-5. 댓글 삭제 (사용자가 쓴 댓글)
        DELETE FROM comments WHERE user_id = :userId;
        
        -- 2-6. 사용자 게시글에 달린 댓글들 삭제
        DELETE FROM comments WHERE post_id IN (
            SELECT id FROM community_posts WHERE user_id = :userId
        );
        
        -- 2-7. 마지막으로 게시글 삭제
        DELETE FROM community_posts WHERE user_id = :userId;
        
        -- 3. 랭킹 데이터 삭제
        DELETE FROM user_rankings WHERE user_id = :userId;
        
        -- 4. 프로필 데이터 삭제
        DELETE FROM user_profiles WHERE user_id = :userId;
        
        -- 5. 마지막으로 사용자 삭제
        DELETE FROM users WHERE user_id = :userId;
        """, nativeQuery = true)
    void deleteUserRelatedData(@Param("userId") Long userId);

}