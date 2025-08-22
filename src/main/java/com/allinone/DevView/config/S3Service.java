package com.allinone.DevView.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    // 버킷 이름 하드코딩 (테스트용)
    private static final String BUCKET_NAME = "devview-allinone";
    private static final String REGION = "ap-northeast-2";

    /**
     * S3에 파일 업로드
     * @param file 업로드할 파일
     * @param folder S3 내 폴더 경로 (예: "profile/123")
     * @return S3 URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFileName = file.getOriginalFilename();
        String fileName = generateFileName(originalFileName);
        String key = folder + "/" + fileName;

        try {
            // 파일 메타데이터 설정 (ACL 제거)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // S3에 업로드
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드된 파일의 URL 반환
            String url = generateFileUrl(key);
            log.info("파일 업로드 성공: {}", url);
            return url;

        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", key, e);
            throw new RuntimeException("S3 파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * S3에서 파일 삭제
     * @param fileUrl 삭제할 파일의 S3 URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key == null) {
                log.warn("URL에서 S3 키를 추출할 수 없습니다: {}", fileUrl);
                return;
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("파일 삭제 성공: {}", key);

        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", fileUrl, e);
            throw new RuntimeException("S3 파일 삭제에 실패했습니다.", e);
        }
    }

    /**
     * 파일 존재 여부 확인
     * @param fileUrl S3 파일 URL
     * @return 존재 여부
     */
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key == null) return false;

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("파일 존재 확인 실패: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateFileName(String originalFileName) {
        if (originalFileName == null) {
            return UUID.randomUUID().toString();
        }

        String extension = "";
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFileName.substring(lastDot);
        }

        return UUID.randomUUID() + extension;
    }

    /**
     * S3 파일 URL 생성
     */
    private String generateFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                BUCKET_NAME, REGION, key);
    }

    /**
     * S3 URL에서 키(key) 추출
     */
    private String extractKeyFromUrl(String fileUrl) {
        try {
            // https://bucket.s3.region.amazonaws.com/key 형태에서 key 부분 추출
            String pattern = String.format("https://%s.s3.%s.amazonaws.com/",
                    BUCKET_NAME, REGION);

            if (fileUrl.startsWith(pattern)) {
                return fileUrl.substring(pattern.length());
            }

            return null;
        } catch (Exception e) {
            log.error("URL에서 키 추출 실패: {}", fileUrl, e);
            return null;
        }
    }
}