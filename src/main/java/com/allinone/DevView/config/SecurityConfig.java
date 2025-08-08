package com.allinone.DevView.config;

import com.allinone.DevView.security.OAuth2SuccessHandler;
import com.allinone.DevView.user.dto.response.UserResponse;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 세션 키 상수 (OAuth2SuccessHandler와 동일)
    private static final String LOGIN_USER = "loginUser";

    /**
     * 비밀번호 암호화에 사용할 인코더 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API 사용)
                .csrf(csrf -> csrf.disable())

                // 세션 관리 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 필요 시 세션 생성
                        .maximumSessions(1) // 동시 세션 1개 제한
                        .maxSessionsPreventsLogin(false) // 새 로그인 시 이전 세션 종료
                )

                // 폼 로그인 설정 (일반 로그인)
                .formLogin(form -> form
                        .loginPage("/user/login") // 커스텀 로그인 페이지 지정
                        .loginProcessingUrl("/api/users/login") // 로그인 처리 URL
                        .usernameParameter("email") // 이메일을 username으로 사용
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler()) // 커스텀 성공 핸들러
                        .failureHandler(customAuthenticationFailureHandler()) // 커스텀 실패 핸들러
                        .permitAll()
                )

                // 기본 인증 비활성화
                .httpBasic(basic -> basic.disable())

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login") // 커스텀 로그인 페이지
                        .successHandler(oAuth2SuccessHandler) // 성공 핸들러
                        .failureUrl("/user/login?error=oauth2") // 실패 시 리다이렉트
                )

                // URL 인가 정책 설정
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 허용
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        // Swagger UI 관련 경로 허용
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // 인증 없이 접근 가능한 페이지/API
                        .requestMatchers("/", "/user/login", "/user/register").permitAll()
                        .requestMatchers("/api/users/register", "/api/users/check-email", "/api/users/check-username").permitAll()
                        .requestMatchers("/login/oauth2/code/**", "/oauth2/**").permitAll()
                        // Swagger 테스트용 임시조치
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/v1/interviews/**").permitAll()
                        // 나머지 요청은 인증 필요
                        .requestMatchers(
                                "/swagger*/",     // swagger로 시작하는 모든 경로
                                "/v3/",          // OpenAPI 3.0 문서
                                "/webjars/**"      // Swagger UI 리소스
                        ).permitAll()
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

    /**
     * 일반 로그인 성공 핸들러
     * JSON 응답 반환 + 세션에 사용자 정보 저장
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            try {
                // 1. 인증된 사용자의 이메일 가져오기
                String email = authentication.getName();

                // 2. UserRepository를 통해 사용자 정보 조회 (순환 참조 방지)
                Optional<User> userOptional = userRepository.findByEmail(email);

                if (userOptional.isEmpty()) {
                    // 사용자를 찾을 수 없는 경우 에러 응답
                    response.setStatus(500);
                    response.setContentType("application/json;charset=UTF-8");

                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "사용자 정보를 찾을 수 없습니다.");

                    response.getWriter().write(objectMapper.writeValueAsString(result));
                    return;
                }

                User user = userOptional.get();
                UserResponse userResponse = UserResponse.from(user);

                // 3. 세션에 사용자 정보 저장 (OAuth2와 동일한 키 사용)
                HttpSession session = request.getSession();
                session.setAttribute(LOGIN_USER, userResponse);

                // 4. JSON 응답 반환
                response.setStatus(200);
                response.setContentType("application/json;charset=UTF-8");

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "로그인 성공");
                result.put("email", email);
                result.put("username", userResponse.getUsername());

                response.getWriter().write(objectMapper.writeValueAsString(result));

                System.out.println("로컬 로그인 성공 - 세션 저장 완료: userId=" + userResponse.getUserId() + ", email=" + email);

            } catch (Exception e) {
                // 예외 발생 시 에러 응답
                response.setStatus(500);
                response.setContentType("application/json;charset=UTF-8");

                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "로그인 처리 중 오류가 발생했습니다.");

                response.getWriter().write(objectMapper.writeValueAsString(result));

                System.err.println("로컬 로그인 핸들러 오류: " + e.getMessage());
            }
        };
    }

    /**
     * 일반 로그인 실패 핸들러
     * JSON 에러 응답 반환
     */
    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");

            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }
}