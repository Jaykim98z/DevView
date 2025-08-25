package com.allinone.DevView.config;

import com.allinone.DevView.security.handler.OAuth2SuccessHandler;
import com.allinone.DevView.security.handler.LocalLoginFailureHandler;
import com.allinone.DevView.security.handler.LocalLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring Security 보안 설정
 * 인증, 인가, 세션 관리 등의 보안 정책 정의
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 사용을 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final LocalLoginSuccessHandler localLoginSuccessHandler;
    private final LocalLoginFailureHandler localLoginFailureHandler;

    /**
     * 비밀번호 암호화 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 설정 - 모든 상태 변경 요청에 적용
                .csrf(csrf -> csrf
                                // 쿠키 기반 CSRF 토큰 저장소 사용 (JavaScript에서 접근 가능)
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // ✅ AI 인터뷰 API 예외 제거 - 모든 API에 CSRF 보호 적용
                        // .ignoringRequestMatchers() 주석 처리 - 모든 POST/PUT/DELETE 요청에 CSRF 필요
                )
                .httpBasic(basic -> basic.disable())

                // 세션 관리
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId()  // 로그인 시 세션 ID만 변경 (속성은 유지)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/api/users/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(localLoginSuccessHandler)
                        .failureHandler(localLoginFailureHandler)
                        .permitAll()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login")
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/user/login?error=oauth2")
                )

                // URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // 문서화 도구(개발과정에서만 주석해제)
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()

                        // 인증 없이 접근 가능한 페이지
                        .requestMatchers("/", "/user/login", "/user/register").permitAll()

                        // 인증 없이 접근 가능한 API (GET 요청만)
                        .requestMatchers("/api/users/register", "/api/users/check-email", "/api/users/check-username").permitAll()

                        // OAuth2 관련
                        .requestMatchers("/login/oauth2/code/**", "/oauth2/**").permitAll()

                        // AI 인터뷰 API 인증 필요 (CSRF 보호 포함)
                        .requestMatchers("/api/v1/interviews/**").authenticated()

                        .requestMatchers("/api/community/posts/**").authenticated() // 커뮤니티 글 수정/삭제 API는 로그인 필요

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")  // CSRF 토큰 쿠키도 삭제
                );

        return http.build();
    }
}