package com.allinone.DevView.community.service;

import com.allinone.DevView.community.dto.PostResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommunityService {

    public List<PostResponseDto> getAllPosts() {
        List<PostResponseDto> postList = new ArrayList<>();

        postList.add(PostResponseDto.builder()
                .id(1L)
                .title("AI 모의면접 후기")
                .writerName("김개발")
                .summary("이력서 기반 질문이 좋았어요")
                .category("BACKEND")
                .level("JUNIOR")
                .type("기술면접")
                .score(85)
                .grade("B")
                .viewCount(120)
                .likeCount(15)
                .scrapCount(3)
                .build());

        postList.add(PostResponseDto.builder()
                .id(2L)
                .title("SI 기업 1차 면접 후기")
                .writerName("이코더")
                .summary("자바 기초 문제 출제됨")
                .category("FRONTEND")
                .level("MID")
                .type("기술면접")
                .score(70)
                .grade("C")
                .viewCount(50)
                .likeCount(10)
                .scrapCount(2)
                .build());

        return postList;
    }
}
