package com.allinone.DevView.mypage.controller;

import com.allinone.DevView.common.util.SecurityUtils;
import com.allinone.DevView.mypage.dto.MypageResponseDto;
import com.allinone.DevView.mypage.dto.UserProfileUpdateRequest;
import com.allinone.DevView.mypage.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class MypageEditController {

    private final MypageService mypageService;

    /** 에딧 페이지 진입 후 기본 프로필 조회 */
    @Operation(summary = "기본 프로필 조회", description = "마이페이지 에딧 진입 시 기본 프로필 정보를 조회합니다.")
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MypageResponseDto> getProfile() {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(mypageService.getBasicUserInfo(userId));
    }

    /** 프로필 저장(이미지 포함) - multipart/form-data (PUT/POST 허용) */
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
                                    value = "{ \"profile\": \"{\\\"name\\\":\\\"홍길동\\\",\\\"job\\\":\\\"Backend Engineer\\\",\\\"careerLevel\\\":\\\"SENIOR\\\"}\", \"profileImage\": \"(파일 업로드)\" }"
                            )
                    )
            )
    )
    @RequestMapping(
            value = "/profile",
            method = {RequestMethod.PUT, RequestMethod.POST},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MypageResponseDto> updateProfileMultipart(
            @Parameter(description = "프로필 정보 JSON", required = true,
                    examples = @ExampleObject(
                            name = "프로필 JSON 예시",
                            value = """
                                    {
                                      "name": "홍길동",
                                      "job": "Backend Engineer",
                                      "careerLevel": "SENIOR",
                                      "bio": "Java/Spring 기반 백엔드 개발자입니다.",
                                      "location": "Seoul",
                                      "skills": ["Java", "Spring Boot", "JPA", "AWS"]
                                    }
                                    """
                    ))
            @RequestPart("profile") @Valid UserProfileUpdateRequest profile,
            @Parameter(description = "프로필 이미지 파일(JPG/PNG, 5MB 이하)", required = false)
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(mypageService.updateProfile(userId, profile, profileImage));
    }

    /** 프로필 저장(이미지 없이) - application/json (PUT/POST 허용) */
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
                                              "job": "Backend Engineer",
                                              "careerLevel": "MID",
                                              "bio": "도메인 주도 설계를 좋아합니다.",
                                              "location": "Seoul",
                                              "skills": ["Java", "Spring Boot", "MySQL"]
                                            }
                                            """
                            )
                    )
            )
    )
    @RequestMapping(
            value = "/profile",
            method = {RequestMethod.PUT, RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MypageResponseDto> updateProfileJson(@Valid @RequestBody UserProfileUpdateRequest profile) {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(mypageService.updateProfile(userId, profile, null));
    }

    /** 프로필 이미지 삭제 */
    @Operation(summary = "프로필 이미지 삭제", description = "등록된 프로필 이미지를 삭제합니다.")
    @DeleteMapping(value = "/profile/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MypageResponseDto> deleteProfileImage() {
        Long userId = SecurityUtils.getUserId();
        return ResponseEntity.ok(mypageService.deleteProfileImage(userId));
    }
}
