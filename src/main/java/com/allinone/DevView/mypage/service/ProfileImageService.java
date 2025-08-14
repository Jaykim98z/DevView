package com.allinone.DevView.mypage.service;

import com.allinone.DevView.mypage.util.ImageFileUtils;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

    private final UserRepository userRepository;

    /** 애플리케이션 설정: 실제 저장 루트 (기본값: 실행 디렉터리 하위 "uploads") */
    @Value("${app.upload-dir:uploads}")
    private String uploadRoot; // 예) ${user.home}/devview_uploads

    /** 웹에서 접근할 정적 URL 프리픽스 */
    private static final String URL_PREFIX = "/uploads/profile/";

    /** 허용 확장자 & 사이즈(5MB) */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    /**
     * 프로필 이미지 업로드(파일시스템 저장만). User 엔티티 업데이트는 호출측(Service)에서 처리.
     * @return 브라우저에서 접근 가능한 URL (예: /uploads/profile/{userId}/{filename})
     */
    public String uploadProfileImage(Long userId, MultipartFile imageFile) {
        validateUser(userId);
        validateFile(imageFile);

        final String originalName = imageFile.getOriginalFilename();
        final String ext = ImageFileUtils.getFileExtension(originalName).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않은 이미지 형식입니다. (jpg, jpeg, png, webp)");
        }

        final String filename = UUID.randomUUID() + "." + ext;

        // 실제 저장 경로 (파일시스템 절대경로): {uploadRoot}/profile/{userId}/{filename}
        final Path userDir = Paths.get(uploadRoot, "profile", String.valueOf(userId))
                .toAbsolutePath()
                .normalize();
        final Path target = userDir.resolve(filename);

        try {
            // 디렉터리 보장
            Files.createDirectories(userDir);

            // 안전 저장: transferTo 대신 스트림 복사 + REPLACE_EXISTING
            try (InputStream in = imageFile.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("Saved profile image to {}", target);
        } catch (IOException e) {
            log.error("Failed to save profile image: {}", target, e);
            throw new RuntimeException("프로필 이미지 저장에 실패했습니다.", e);
        }

        // 반환 URL: /uploads/profile/{userId}/{filename}
        return URL_PREFIX + userId + "/" + filename;
    }

    /**
     * 파일 URL로 이미지 삭제 (파일 없으면 조용히 무시)
     * 기대 URL: /uploads/profile/{userId}/{filename}
     */
    public void deleteProfileImage(Long userId, String imageUrl) {
        validateUser(userId);
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        // URL → 로컬 파일 경로: {uploadRoot}/profile/{userId}/{filename}
        // "/uploads/" 프리픽스를 떼고 uploadRoot 밑으로 매핑
        final String url = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl; // uploads/profile/...
        final String relative = url.replaceFirst("^uploads/", "");                      // profile/...
        final Path filePath = Paths.get(uploadRoot).toAbsolutePath().normalize()
                .resolve(relative)
                .normalize();

        try {
            Files.deleteIfExists(filePath);

            // (선택) 부모 디렉터리 정리 시도: 비어있지 않으면 자동 무시
            final Path parent = filePath.getParent();
            if (parent != null) {
                try { Files.delete(parent); } catch (Exception ignore) { /* no-op */ }
            }
        } catch (IOException e) {
            log.warn("Failed to delete profile image: {}", filePath, e);
            throw new RuntimeException("프로필 이미지 삭제에 실패했습니다.", e);
        }
    }

    /* ===================== 내부 유틸 ===================== */

    private void validateUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("파일 용량은 5MB 이하만 허용됩니다.");
        }
        if (!ImageFileUtils.isImageFile(file)) {
            throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다.");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("파일명이 비어있습니다.");
        }
    }
}
