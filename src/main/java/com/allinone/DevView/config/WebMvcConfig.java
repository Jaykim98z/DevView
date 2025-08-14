package com.allinone.DevView.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * ProfileImageService 와 동일한 프로퍼티를 사용합니다.
     * application.yml에 없으면 실행 디렉토리 하위 "uploads"가 기본값입니다.
     *
     * 예) application.yml
     * app:
     *   upload-dir: ${user.home}/devview_uploads
     */
    @Value("${app.upload-dir:uploads}")
    private String uploadRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 파일시스템 경로 (절대경로로 정규화)
        Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();

        // file: 스킴으로 정적 리소스 매핑 (/uploads/** → 실제 {uploadRoot}/**)
        // 뒤에 슬래시 꼭 붙입니다.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + root + "/");
        // .setCachePeriod(3600);  // 원하면 캐시 시간 설정
        // .resourceChain(true);   // 필요 시 리소스 체인 활성화
    }
}
