package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.User;
import com.brewnow.service.MerchantService;
import com.brewnow.service.MinioStorageService;
import com.brewnow.service.UserService;
import com.brewnow.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private MerchantService merchantService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MinioStorageService minioStorageService;

    @InjectMocks
    private UserController userController;

    @Test
    void loginShouldReturnTokenAndUserInfo() {
        User user = new User();
        user.setUserId(7);
        user.setUsername("tea-user");

        when(userService.login("tea_account", "123456")).thenReturn("mock-token");
        when(jwtUtil.getUserIdFromToken("mock-token")).thenReturn(7);
        when(userService.getUserById(7)).thenReturn(user);

        Result<Map<String, Object>> result = userController.login(Map.of(
                "account", "tea_account",
                "password", "123456"
        ));

        assertEquals(200, result.getCode());
        assertEquals("mock-token", result.getData().get("token"));
        assertNotNull(result.getData().get("userInfo"));
    }
}
