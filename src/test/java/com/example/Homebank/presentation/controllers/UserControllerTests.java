package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.UserService;
import com.example.Homebank.presentation.dto.auth.ChangePasswordDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController controller;

    @Test
    void changePassword_validPayload_returnsOkAndCallsService() throws Exception {
        var mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mockMvc.perform(post("/users/changePassword").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"old-password","newPassword":"new-password",
                                 "confirmNewPassword":"new-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userService).changePassword(new ChangePasswordDTO("old-password", "new-password", "new-password"));
        verifyNoMoreInteractions(userService);
    }
}
