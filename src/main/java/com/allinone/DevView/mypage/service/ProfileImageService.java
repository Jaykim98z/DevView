package com.allinone.DevView.mypage.service;

import com.allinone.DevView.user.repository.UserRepository;
import com.allinone.DevView.mypage.util.ImageFileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final UserRepository userRepository;

    /** 웹에서 접근할 정적 URL 프리픽스 */
    private static final String URL_PREFIX = "/uploads/profile/";
    /** 실제 저장 루트 (애플리케이션 실행 디렉터리 기준) */
    private static final Path UPLOAD_ROOT = Paths.get("uploads", "profile");

    /** 허용 확장자 & 사이즈(5MB) */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    /**
     * 프로필 이미지 업로드만 수행 (User 엔티티에는 저장하지 않음)
     * @return 웹에서 접근 가능한 URL (예: /uploads/profile/{userId}/{filename})
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
        final Path userDir = UPLOAD_ROOT.resolve(String.valueOf(userId));
        final Path target = userDir.resolve(filename);

        try {
            Files.createDirectories(userDir);
            imageFile.transferTo(target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 저장에 실패했습니다.", e);
        }

        // 반환 URL: /uploads/profile/{userId}/{filename}
        return URL_PREFIX + userId + "/" + filename;
    }

    /**
     * 파일 경로(웹 URL) 기반으로 이미지 삭제만 수행
     */
    public void deleteProfileImage(Long userId, String imageUrl) {
        validateUser(userId);
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        // 기대 URL: /uploads/profile/{userId}/{filename}
        // 로컬 경로: uploads/profile/{userId}/{filename}
        final String normalized = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
        final Path localPath = Paths.get(normalized.replace("\\", "/"));

        try {
            if (Files.exists(localPath) && Files.isRegularFile(localPath)) {
                Files.delete(localPath);
            }
        } catch (IOException e) {
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
