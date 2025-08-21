package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.service.MypageService;
import com.allinone.DevView.user.dto.response.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "마이페이지 편집", description = "프로필 조회/수정 및 이미지 삭제 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/mypage")
@Slf4j
public class MypageEditController {

    private final MypageService mypageService;
    private final ObjectMapper objectMapper;

    /**
     * 세션에서 사용자 ID 가져오기 (인증 확인 포함)
     */
    private Long getUserIdFromSession(HttpSession session) {
        UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return loginUser.getUserId();
    }

    /**
     * 세션의 사용자 정보 업데이트
     */
    private void updateSessionUserInfo(HttpSession session, Long userId) {
        try {
            // 최신 사용자 정보로 세션 업데이트
            MypageResponseDto updatedUserInfo = mypageService.getBasicUserInfo(userId);
            UserResponse updatedUserResponse = UserResponse.builder()
                    .userId(userId)
                    .username(updatedUserInfo.getName())
                    .email(updatedUserInfo.getEmail())
                    .build();

            session.setAttribute("loginUser", updatedUserResponse);
            log.info("세션 사용자 정보 업데이트 완료: userId={}, newName={}", userId, updatedUserInfo.getName());
        } catch (Exception e) {
            log.warn("세션 업데이트 실패: userId={}", userId, e);
            // 세션 업데이트 실패해도 프로필 수정은 성공했으므로 계속 진행
        }
    }

    /** 에딧 페이지 진입 후 기본 프로필 조회 */
    @Operation(summary = "기본 프로필 조회", description = "마이페이지 에딧 진입 시 기본 프로필 정보를 조회합니다.")
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MypageResponseDto> getProfile(HttpSession session) {
        Long userId = getUserIdFromSession(session);
        return ResponseEntity.ok(mypageService.getBasicUserInfo(userId));
    }

    /** 프로필 저장(이미지 포함)*/
    @Operation(
            summary = "프로필 저장 (multipart)",
            description = "프로필 정보(JSON)와 프로필 이미지 파일을 함께 업로드합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(description = "멀티파트 폼 데이터(프로필 JSON + 이미지 파일)"),
                            examples = @ExampleObject(
                                    name = "multipart 예시",
                                    description = "form-data 필드 예시",
                                    value = "{ \"profile\": \"{\\\"name\\\":\\\"홍길동\\\",\\\"job\\\":\\\"BACKEND\\\",\\\"careerLevel\\\":\\\"SENIOR\\\",\\\"selfIntroduction\\\":\\\"자기소개 내용\\\"}\", \"profileImage\": \"(파일 업로드)\" }"
                            )
                    )
            )
    )
    @PostMapping(
            value = "/profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MypageResponseDto> updateProfileMultipart(
            @Parameter(description = "프로필 정보 JSON (application/json 문자열)", required = true)
            @RequestPart("profile") String profileJson,
            @Parameter(description = "프로필 이미지 파일(JPG/PNG, 5MB 이하)", required = false)
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpSession session
    ) {
        Long userId = getUserIdFromSession(session);

        // 1) JSON 파트 로깅(트러블슈팅용, 민감정보 없음 가정)
        if (log.isDebugEnabled()) {
            log.debug("updateProfileMultipart - profileJson: {}", profileJson);
            log.debug("updateProfileMultipart - image present: {}", (profileImage != null && !profileImage.isEmpty()));
        }

        // 2) 컨트롤러에서 직접 파싱하여 400/명확 메시지로 전환
        final UserProfileUpdateRequest profile;
        try {
            profile = objectMapper.readValue(profileJson, UserProfileUpdateRequest.class);
        } catch (JsonProcessingException e) {
            // 바인딩 실패를 500로 올리지 말고 400으로 명확히 반환
            throw new IllegalArgumentException("잘못된 프로필 형식입니다. 유효한 JSON을 전달해 주세요.", e);
        }

        // 3) 서비스 호출
        MypageResponseDto result = mypageService.updateProfile(userId, profile, profileImage);

        // 4) 세션 정보 업데이트 (헤더 닉네임 즉시 반영)
        updateSessionUserInfo(session, userId);

        return ResponseEntity.ok(result);
    }

    /** 프로필 저장(이미지 없이)*/
    @Operation(
            summary = "프로필 저장 (JSON)",
            description = "이미지 없이 프로필 정보만 JSON으로 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "JSON 예시",
                                    value = """
                                            {
                                              "name": "홍길동",
                                              "job": "BACKEND",
                                              "careerLevel": "MID",
                                              "selfIntroduction": "Java Spring 기반 백엔드 개발 3년 경험. AWS 클라우드 환경에서의 MSA 설계 및 운영 경험이 있습니다."
                                            }
                                            """
                            )
                    )
            )
    )
    @PutMapping(
            value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MypageResponseDto> updateProfileJson(
            @Valid @RequestBody UserProfileUpdateRequest profile,
            HttpSession session
    ) {
        Long userId = getUserIdFromSession(session);
        MypageResponseDto result = mypageService.updateProfile(userId, profile, null);

        // 세션 정보 업데이트 (헤더 닉네임 즉시 반영)
        updateSessionUserInfo(session, userId);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "프로필 이미지 삭제", description = "등록된 프로필 이미지를 삭제합니다.")
    @DeleteMapping(value = "/profile/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MypageResponseDto> deleteProfileImage(HttpSession session) {
        Long userId = getUserIdFromSession(session);
        return ResponseEntity.ok(mypageService.deleteProfileImage(userId));
    }

    /**
     * 마이페이지 메인 데이터 조회 (JavaScript에서 호출)
     * GET /api/mypage
     */
    @Operation(summary = "마이페이지 데이터 조회", description = "면접 목록, 스크랩 등 마이페이지 메인 데이터를 JSON으로 반환합니다.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MypageResponseDto> getMypageData(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            MypageResponseDto data = mypageService.getMypageData(userId);

            log.info("마이페이지 데이터 조회 성공: userId={}, interviews={}",
                    userId, data.getInterviews().size());

            return ResponseEntity.ok(data);
        } catch (IllegalStateException e) {
            log.warn("마이페이지 데이터 조회 실패 - 인증 필요");
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("마이페이지 데이터 조회 중 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

}