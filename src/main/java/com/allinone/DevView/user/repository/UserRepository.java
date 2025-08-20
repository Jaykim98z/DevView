package com.allinone.DevView.user.repository;

import com.allinone.DevView.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 데이터 접근 계층
 * ✅ 개선사항: provider별로 사용자를 구분하여 조회할 수 있는 메서드 추가
 * - LOCAL 사용자와 GOOGLE 사용자를 명확히 분리
 * - provider_id를 활용한 정확한 OAuth2 사용자 식별
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =====================================================================
    // 기본 조회 메서드들 (기존 유지)
    // =====================================================================

    /**
     * 이메일로 사용자 찾기 (모든 provider 포함)
     * ⚠️ 주의: 같은 이메일에 LOCAL/GOOGLE이 모두 있을 수 있으므로 사용 시 주의 필요
     * 가능하면 provider별 조회 메서드 사용 권장
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자명 중복 확인 (회원가입 시 사용)
     * 모든 provider에서 사용자명은 유니크해야 함
     */
    boolean existsByUsername(String username);

    // =====================================================================
    // ✅ 개선된 Provider별 조회 메서드들
    // =====================================================================

    /**
     * OAuth2 사용자 찾기 (provider + provider_id 조합으로 정확한 식별)
     * 구글 로그인 시 사용 - provider_id가 구글의 고유 ID(sub)
     */
    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.providerId = :providerId")
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider,
                                               @Param("providerId") String providerId);

    /**
     * LOCAL 사용자만 찾기 (이메일 기준)
     * 로컬 로그인, 비밀번호 변경 등에서 사용
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND (u.provider = 'LOCAL' OR u.provider IS NULL)")
    Optional<User> findLocalUserByEmail(@Param("email") String email);

    /**
     * 특정 provider + email 조합으로 사용자 찾기
     * 범용 조회 메서드
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.provider = :provider")
    Optional<User> findByEmailAndProvider(@Param("email") String email, @Param("provider") String provider);

    /**
     * 특정 provider + email 조합의 존재 여부 확인
     * 이메일 중복 확인 시 provider별로 체크할 때 사용
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.provider = :provider")
    boolean existsByEmailAndProvider(@Param("email") String email, @Param("provider") String provider);

    // =====================================================================
    // 통계 및 분석용 조회 메서드들
    // =====================================================================

    /**
     * 전체 사용자 수 조회 (provider별)
     */
    @Query("SELECT u.provider, COUNT(u) FROM User u GROUP BY u.provider")
    List<Object[]> countUsersByProvider();

    /**
     * 특정 provider 사용자 목록 조회
     * 관리자 페이지나 분석용
     */
    @Query("SELECT u FROM User u WHERE u.provider = :provider ORDER BY u.createdAt DESC")
    List<User> findByProvider(@Param("provider") String provider);

    /**
     * 최근 가입한 사용자 조회 (provider별)
     */
    @Query("SELECT u FROM User u WHERE u.provider = :provider ORDER BY u.createdAt DESC")
    List<User> findRecentUsersByProvider(@Param("provider") String provider,
                                         org.springframework.data.domain.Pageable pageable);

    // =====================================================================
    // ⚠️ 레거시 메서드 (호환성 유지, 가능하면 사용 지양)
    // =====================================================================

    /**
     * 이메일 중복 확인 (모든 provider 포함)
     * ⚠️ 레거시: 새로운 코드에서는 existsByEmailAndProvider() 사용 권장
     */
    boolean existsByEmail(String email);

    // =====================================================================
    // 회원탈퇴 관련 메서드
    // =====================================================================

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
            SELECT post_id FROM community_posts WHERE user_id = :userId
        );
        
        -- 2-3. 좋아요 삭제 (사용자가 누른 것들)
        DELETE FROM likes WHERE user_id = :userId;
        
        -- 2-4. 사용자 게시글에 달린 좋아요들 삭제
        DELETE FROM likes WHERE post_id IN (
            SELECT post_id FROM community_posts WHERE user_id = :userId
        );
        
        -- 2-5. 댓글 삭제 (사용자가 쓴 댓글)
        DELETE FROM comments WHERE user_id = :userId;
        
        -- 2-6. 사용자 게시글에 달린 댓글들 삭제
        DELETE FROM comments WHERE post_id IN (
            SELECT post_id FROM community_posts WHERE user_id = :userId
        );
        
        -- 2-7. 게시글 삭제
        DELETE FROM community_posts WHERE user_id = :userId;
        
        -- 3. 사용자 프로필 삭제 (1:1 관계, CASCADE로 자동 삭제되지만 명시적 처리)
        DELETE FROM user_profiles WHERE user_id = :userId;
        
        -- 4. 사용자 랭킹 삭제
        DELETE FROM user_rankings WHERE user_id = :userId;
        
        -- 5. 마지막으로 사용자 삭제
        DELETE FROM users WHERE user_id = :userId;
        """, nativeQuery = true)
    void deleteUserRelatedData(@Param("userId") Long userId);
}