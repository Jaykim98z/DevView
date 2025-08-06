package com.devview.mypage.controller;

import com.devview.mypage.service.ProfileImageService;
import com.devview.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MyPageController.class)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ProfileImageService profileImageService;

    @Test
    @DisplayName("마이페이지 진입 성공")
    void showMyPage_success() throws Exception {
        // given
        User mockUser = User.builder()
                .id(1L)
                .name("테스터")
                .email("test@example.com")
                .job("개발자")
                .careerLevel("주니어")
                .build();

        when(userService.getUserById(1L)).thenReturn(mockUser);

        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("1");

        // when & then
        mockMvc.perform(get("/mypage").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("마이페이지 수정 폼 진입 성공")
    void showEditForm_success() throws Exception {
        User mockUser = User.builder()
                .id(1L)
                .name("테스터")
                .email("test@example.com")
                .build();

        when(userService.getUserById(1L)).thenReturn(mockUser);

        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("1");

        mockMvc.perform(get("/mypage/edit").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/mypage-edit"))
                .andExpect(model().attributeExists("user"));
    }
}
