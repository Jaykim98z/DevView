package com.devview.mypage.entity;

import com.devview.user.entity.User; // ✅ 정확한 경로로 import 추가
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scraps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Scrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;        // 콘텐츠 제목

    private String link;         // 외부 또는 내부 링크

    private int likeCount;

    private int commentCount;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // ✅ user 엔티티는 user 패키지의 User로 변경
}
