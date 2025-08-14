package com.allinone.DevView.ranking.controller;

import com.allinone.DevView.ranking.dto.response.RankingResponse;
import com.allinone.DevView.ranking.service.RankingService;
import com.allinone.DevView.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 랭킹 페이지 뷰 컨트롤러
 * HTML 페이지 렌더링 담당
 */
@Slf4j
@Controller
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingViewController {

    private final RankingService rankingService;

    /**
     * 랭킹 페이지 렌더링
     * GET /ranking/
     */
    @GetMapping({"", "/"})
    public String rankingPage(Model model, HttpSession session) {
        log.info("랭킹 페이지 요청");

        try {
            // 상위 3명 데이터 조회
            List<RankingResponse> top3Rankings = rankingService.getTop3Rankings();
            model.addAttribute("top3Rankings", top3Rankings);
            log.info("상위 3명 데이터 조회 완료: count={}", top3Rankings.size());

            // 상위 20명 데이터 조회
            List<RankingResponse> top20Rankings = rankingService.getTop20Rankings();
            model.addAttribute("top20Rankings", top20Rankings);
            log.info("상위 20명 데이터 조회 완료: count={}", top20Rankings.size());

            // 전체 사용자 수
            Long totalUsers = (long) top20Rankings.size(); // 또는 별도 조회
            model.addAttribute("totalUsers", totalUsers);

            // 현재 로그인한 사용자 정보 (선택사항)
            UserResponse loginUser = (UserResponse) session.getAttribute("loginUser");
            if (loginUser != null) {
                model.addAttribute("currentUserId", loginUser.getUserId());
                log.info("현재 로그인 사용자: userId={}", loginUser.getUserId());
            }

            return "ranking/ranking"; // templates/ranking/ranking.html 렌더링

        } catch (Exception e) {
            log.error("랭킹 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorMessage", "랭킹 데이터를 불러오는 중 오류가 발생했습니다.");
            return "ranking/ranking";
        }
    }
}