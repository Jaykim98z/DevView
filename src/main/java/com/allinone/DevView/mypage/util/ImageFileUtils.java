package com.allinone.DevView.mypage.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public final class ImageFileUtils {

    private ImageFileUtils() { /* 유틸 클래스 인스턴스화 방지 */ }

    // ProfileImageService와 일관되게 유지 (jpg/jpeg/png/webp)
    private static final Set<String> VALID_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    /**
     * 파일의 확장자를 추출합니다.
     * @param originalFileName 업로드된 파일명
     * @return 확장자 (소문자)
     */
    public static String getFileExtension(String originalFileName) {
        if (originalFileName == null) {
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
        }
        String trimmed = originalFileName.trim();
        int dot = trimmed.lastIndexOf('.');
        if (dot < 0 || dot == trimmed.length() - 1) {
            throw new IllegalArgumentException("파일 확장자가 존재하지 않습니다.");
        }
        return trimmed.substring(dot + 1).toLowerCase();
    }

    /**
     * 업로드된 파일이 이미지인지 판단합니다.
     * (확장자 화이트리스트 + MIME 타입 프리픽스 동시 점검)
     * @param file MultipartFile 객체
     * @return 이미지 파일 여부
     */
    public static boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        boolean mimeOk = contentType != null && contentType.toLowerCase().startsWith("image/");
        String extension = getFileExtension(file.getOriginalFilename());
        boolean extOk = VALID_EXTENSIONS.contains(extension);
        return mimeOk && extOk;
    }
}
