package com.allinone.DevView.config;

import com.allinone.DevView.security.handler.OAuth2SuccessHandler;
import com.allinone.DevView.security.handler.LocalLoginFailureHandler;
import com.allinone.DevView.security.handler.LocalLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 보안 설정
 * 인증, 인가, 세션 관리 등의 보안 정책 정의
 */
@Configuration
@EnableWebSecurity
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
                // 기본 보안 설정
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 관리
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
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

                        // 문서화 도구
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()

                        // 인증 없이 접근 가능한 페이지
                        .requestMatchers("/", "/user/login", "/user/register").permitAll()

                        // 인증 없이 접근 가능한 API
                        .requestMatchers("/api/users/register", "/api/users/check-email", "/api/users/check-username").permitAll()

                        // OAuth2 관련
                        .requestMatchers("/login/oauth2/code/**", "/oauth2/**").permitAll()

                        // 임시: 테스트용 API
                        .requestMatchers("/api/v1/interviews/**").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}