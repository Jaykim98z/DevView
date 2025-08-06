package com.allinone.DevView.mypage.service;

import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import com.allinone.DevView.mypage.util.ImageFileUtils;
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

    public void uploadProfileImage(Long userId, MultipartFile imageFile) {
        if (imageFile.isEmpty() || !ImageFileUtils.isImageFile(imageFile)) {
            throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다.");
        }

        User user = getUser(userId);

        String extension = ImageFileUtils.getFileExtension(imageFile.getOriginalFilename());
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

        user.setProfileImageUrl("/" + savePath);
        userRepository.save(user);
    }

    public void deleteProfileImage(Long userId) {
        User user = getUser(userId);
        String path = user.getProfileImageUrl();

        if (path != null) {
            File file = new File("." + path);
            if (file.exists()) {
                file.delete();
            }
        }

        user.setProfileImageUrl(null);
        userRepository.save(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
