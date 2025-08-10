package com.allinone.DevView.mypage.service;

import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final UserRepository userRepository;

    private static final String IMAGE_UPLOAD_DIR = "uploads/profile/";

    /*** 이미지 업로드만 수행 (User 엔티티에는 저장하지 않음)*/
    public String uploadProfileImage(Long userId, MultipartFile imageFile) {
        validateUser(userId);

        if (imageFile.isEmpty() || !com.allinone.DevView.mypage.util.ImageFileUtils.isImageFile(imageFile)) {
            throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다.");
        }

        String extension = com.allinone.DevView.mypage.util.ImageFileUtils.getFileExtension(imageFile.getOriginalFilename());
        String uniqueName = UUID.randomUUID() + "." + extension;
        String savePath = IMAGE_UPLOAD_DIR + uniqueName;

        try {
            File directory = new File(IMAGE_UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            imageFile.transferTo(new File(savePath));
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장에 실패했습니다.", e);
        }

        return "/" + savePath; // 저장된 파일 경로 반환 (예: /uploads/profile/uuid.jpg)
    }

    /*** 파일 경로 기반으로 이미지 삭제만 수행*/
    public void deleteProfileImage(Long userId, String imageUrl) {
        validateUser(userId); // 사용자 존재 확인만

        if (imageUrl != null && !imageUrl.isBlank()) {
            File file = new File("." + imageUrl);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void validateUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));
    }
}
