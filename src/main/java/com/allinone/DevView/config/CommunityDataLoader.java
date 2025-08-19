package com.allinone.DevView.config;

import com.allinone.DevView.common.enums.Grade;
import com.allinone.DevView.community.entity.CommunityPosts;
import com.allinone.DevView.community.service.CommunityService;
import com.allinone.DevView.user.entity.User;
import com.allinone.DevView.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class CommunityDataLoader implements ApplicationRunner {

    private final CommunityService communityService;
    private final UserRepository userRepository;

    private static final String SEED_EMAIL = "seed@devview.local";

    @Override
    public void run(ApplicationArguments args) {
        if (!communityService.getAllPostDtos().isEmpty()) return;

        User user = userRepository.findByEmail(SEED_EMAIL)
                .orElseGet(() -> {
                    return userRepository.findAll().stream().findFirst().orElse(null);
                });

        if (user == null) {
            log.warn("Seed skipped: no user available ({} not found, and user table is empty)", SEED_EMAIL);
            return;
        }

        CommunityPosts post = new CommunityPosts();
        post.setUser(user);
        post.setTitle("Spring 면접 후기");
        post.setContent("MSA 구조 설계, API Gateway, Eureka 질문 나옴");
        post.setGrade(Grade.B);
        post.setScore(88);
        post.setViewCount(123);
        post.setLikeCount(10);
        post.setScrapCount(5);
        post.setInterviewType("PRACTICE");
        post.setCreatedAt(LocalDateTime.now());

        communityService.createPost(post);
        log.info("Seeded 1 community post for dev profile (author email={})", user.getEmail());
    }
}
