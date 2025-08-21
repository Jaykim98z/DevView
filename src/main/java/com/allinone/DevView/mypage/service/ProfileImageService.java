package com.allinone.DevView.mypage.service;

import com.allinone.DevView.config.S3Service;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    /** S3 사용 여부 플래그 */
    @Value("${app.use-s3:true}")
    private boolean useS3;

    /** 기존 로컬 저장 설정 (백업용) */
    @Value("${app.upload-dir:uploads}")
    private String uploadRoot;

    /** 허용 확장자 & 사이즈(5MB) */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    /**
     * 프로필 이미지 업로드 (S3 또는 로컬)
     * @param userId 사용자 ID
     * @param imageFile 업로드할 이미지 파일
     * @return 이미지 URL (S3 URL 또는 로컬 URL)
     */
    public String uploadProfileImage(Long userId, MultipartFile imageFile) {
        validateUser(userId);
        validateFile(imageFile);

        final String originalName = imageFile.getOriginalFilename();
        final String ext = ImageFileUtils.getFileExtension(originalName).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않은 이미지 형식입니다. (jpg, jpeg, png, webp)");
        }

        if (useS3) {
            return uploadToS3(userId, imageFile);
        } else {
            return uploadToLocal(userId, imageFile);
        }
    }

    /**
     * 프로필 이미지 삭제 (S3 또는 로컬)
     * @param userId 사용자 ID
     * @param imageUrl 삭제할 이미지 URL
     */
    public void deleteProfileImage(Long userId, String imageUrl) {
        validateUser(userId);
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        if (useS3 && isS3Url(imageUrl)) {
            deleteFromS3(imageUrl);
        } else {
            deleteFromLocal(imageUrl);
        }
    }

    /* ===================== S3 업로드/삭제 ===================== */

    private String uploadToS3(Long userId, MultipartFile imageFile) {
        try {
            String folder = "profile/" + userId;
            String s3Url = s3Service.uploadFile(imageFile, folder);
            log.info("S3 업로드 성공 - 사용자: {}, URL: {}", userId, s3Url);
            return s3Url;
        } catch (Exception e) {
            log.error("S3 업로드 실패 - 사용자: {}", userId, e);
            throw new RuntimeException("프로필 이미지 업로드에 실패했습니다.", e);
        }
    }

    private void deleteFromS3(String imageUrl) {
        try {
            s3Service.deleteFile(imageUrl);
            log.info("S3 삭제 성공: {}", imageUrl);
        } catch (Exception e) {
            log.warn("S3 삭제 실패: {}", imageUrl, e);
            throw new RuntimeException("프로필 이미지 삭제에 실패했습니다.", e);
        }
    }

    /* ===================== 로컬 업로드/삭제 (기존 코드) ===================== */

    private String uploadToLocal(Long userId, MultipartFile imageFile) {
        final String originalName = imageFile.getOriginalFilename();
        final String ext = ImageFileUtils.getFileExtension(originalName).toLowerCase();
        final String filename = java.util.UUID.randomUUID() + "." + ext;

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

            log.debug("로컬 저장 성공: {}", target);
        } catch (IOException e) {
            log.error("로컬 저장 실패: {}", target, e);
            throw new RuntimeException("프로필 이미지 저장에 실패했습니다.", e);
        }

        // 반환 URL: /uploads/profile/{userId}/{filename}
        return "/uploads/profile/" + userId + "/" + filename;
    }

    private void deleteFromLocal(String imageUrl) {
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
            log.warn("로컬 파일 삭제 실패: {}", filePath, e);
            throw new RuntimeException("프로필 이미지 삭제에 실패했습니다.", e);
        }
    }

    /* ===================== 유틸리티 메서드 ===================== */

    /**
     * S3 URL인지 확인
     */
    private boolean isS3Url(String url) {
        return url != null && url.contains("amazonaws.com");
    }

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