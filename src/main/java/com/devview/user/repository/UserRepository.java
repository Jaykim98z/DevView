package com.devview.user.repository;

import com.devview.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    @Query("SELECT u FROM User u WHERE (u.provider = 'LOCAL' OR u.provider IS NULL) AND u.email = :email")
    Optional<User> findLocalUserByEmail(@Param("email") String email);

    /**
     * 이메일 + 비밀번호로 사용자 찾기 (로컬 로그인용)
     */
    Optional<User> findByEmailAndPassword(String email, String password);
}
