package org.sang.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sang.bean.RespBean;
import org.sang.bean.User;
import org.sang.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LoginRegController 单元测试
 * 测试用户注册功能
 */
@ExtendWith(MockitoExtension.class)
class LoginRegControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private LoginRegController loginRegController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginRegController).build();
    }

    @Test
    @DisplayName("注册成功 - 返回 result=0 时应返回成功响应")
    void reg_WhenSuccess_ShouldReturnSuccessResponse() {
        // Arrange
        User user = createTestUser();
        when(userService.reg(any(User.class))).thenReturn(0);

        // Act
        RespBean result = loginRegController.reg(user);

        // Assert
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertEquals("注册成功!", result.getMsg());
        verify(userService, times(1)).reg(any(User.class));
    }

    @Test
    @DisplayName("用户名重复 - 返回 result=1 时应返回用户名重复错误")
    void reg_WhenUsernameExists_ShouldReturnDuplicateError() {
        // Arrange
        User user = createTestUser();
        when(userService.reg(any(User.class))).thenReturn(1);

        // Act
        RespBean result = loginRegController.reg(user);

        // Assert
        assertNotNull(result);
        assertEquals("error", result.getStatus());
        assertEquals("用户名重复，注册失败!", result.getMsg());
        verify(userService, times(1)).reg(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 返回其他值时应返回注册失败错误")
    void reg_WhenOtherError_ShouldReturnFailureError() {
        // Arrange
        User user = createTestUser();
        when(userService.reg(any(User.class))).thenReturn(2);

        // Act
        RespBean result = loginRegController.reg(user);

        // Assert
        assertNotNull(result);
        assertEquals("error", result.getStatus());
        assertEquals("注册失败!", result.getMsg());
        verify(userService, times(1)).reg(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 返回负数时应返回注册失败错误")
    void reg_WhenNegativeResult_ShouldReturnFailureError() {
        // Arrange
        User user = createTestUser();
        when(userService.reg(any(User.class))).thenReturn(-1);

        // Act
        RespBean result = loginRegController.reg(user);

        // Assert
        assertNotNull(result);
        assertEquals("error", result.getStatus());
        assertEquals("注册失败!", result.getMsg());
        verify(userService, times(1)).reg(any(User.class));
    }

    @Test
    @DisplayName("MockMvc测试 - POST /reg 注册成功")
    void reg_MockMvcTest_WhenSuccess_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.reg(any(User.class))).thenReturn(0);

        // Act & Assert
        mockMvc.perform(post("/reg")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("nickname", "Test User")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.msg").value("注册成功!"));

        verify(userService, times(1)).reg(any(User.class));
    }

    @Test
    @DisplayName("MockMvc测试 - POST /reg 用户名重复")
    void reg_MockMvcTest_WhenUsernameExists_ShouldReturnError() throws Exception {
        // Arrange
        when(userService.reg(any(User.class))).thenReturn(1);

        // Act & Assert
        mockMvc.perform(post("/reg")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "existinguser")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.msg").value("用户名重复，注册失败!"));

        verify(userService, times(1)).reg(any(User.class));
    }

    @Test
    @DisplayName("MockMvc测试 - POST /reg 注册失败")
    void reg_MockMvcTest_WhenFailure_ShouldReturnError() throws Exception {
        // Arrange
        when(userService.reg(any(User.class))).thenReturn(2);

        // Act & Assert
        mockMvc.perform(post("/reg")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testuser")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.msg").value("注册失败!"));

        verify(userService, times(1)).reg(any(User.class));
    }

    /**
     * 创建测试用户
     */
    private User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setNickname("Test User");
        user.setEmail("test@example.com");
        return user;
    }
}

