package com.devview.mypage.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class ImageFileUtils {

    private ImageFileUtils() {} // 유틸 클래스 생성 방지

    private static final List<String> VALID_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    /**
     * 파일의 확장자를 추출합니다.
     *
     * @param originalFileName 업로드된 파일명
     * @return 확장자 (소문자)
     */
    public static String getFileExtension(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new IllegalArgumentException("파일 확장자가 존재하지 않습니다.");
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 업로드된 파일이 이미지인지 판단합니다.
     *
     * @param file MultipartFile 객체
     * @return 이미지 파일 여부
     */
    public static boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        String extension = getFileExtension(file.getOriginalFilename());
        return VALID_EXTENSIONS.contains(extension);
    }
}
