package com.allinone.DevView.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;

@Controller
public class CommunityController {

    @GetMapping("/community")
    public String communityPage(Model model) {
        List<Map<String, Object>> postList = new ArrayList<>();

        Map<String, Object> post1 = new HashMap<>();
        post1.put("id", 1);
        post1.put("writerName", "홍길동");
        post1.put("category", "BACKEND");
        post1.put("level", "JUNIOR");
        post1.put("title", "AI 모의면접 후기");
        post1.put("summary", "AI와 진행한 백엔드 모의면접 경험입니다.");
        post1.put("type", "기술면접");
        post1.put("score", 85);
        post1.put("grade", "B");
        post1.put("viewCount", 120);
        post1.put("likeCount", 15);
        post1.put("scrapCount", 5);
        postList.add(post1);

        Map<String, Object> post2 = new HashMap<>();
        post2.put("id", 2);
        post2.put("writerName", "김영희");
        post2.put("category", "FRONTEND");
        post2.put("level", "MID");
        post2.put("title", "SI 기업 1차 면접 후기");
        post2.put("summary", "1차 실무 면접에서 받은 질문들 공유합니다.");
        post2.put("type", "실무면접");
        post2.put("score", 90);
        post2.put("grade", "A");
        post2.put("viewCount", 45);
        post2.put("likeCount", 7);
        post2.put("scrapCount", 2);
        postList.add(post2);

        model.addAttribute("postList", postList);
        model.addAttribute("sort", "latest");
        model.addAttribute("category", "ALL");
        model.addAttribute("level", "JUNIOR");
        model.addAttribute("query", "");

        return "community/community";
    }

}
