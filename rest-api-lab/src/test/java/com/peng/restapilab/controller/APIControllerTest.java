package com.peng.restapilab.controller;

import com.peng.restapilab.model.User;
import com.peng.restapilab.services.APIServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(APIController.class)
class APIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private APIServices userService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetUsers() throws Exception {
        List<User> users = Arrays.asList(
                new User("2025123", "wupeng", "pengwu"),
                new User("2025124", "max", "max")
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()))
                .andExpect(jsonPath("$[0].id").value("2025123"))
                .andExpect(jsonPath("$[0].name").value("wupeng"));
    }


    @Test
    public void testGetUserById() throws Exception {
        User user = new User("2025123", "wupeng", "pengwu");
        when(userService.getUserById("2025123")).thenReturn(user);

        mockMvc.perform(get("/api/users/2025123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2025123"))
                .andExpect(jsonPath("$.name").value("wupeng"));
    }

    @Test
    public void testCreateUser() throws Exception {
        User user = new User("2025123", "wupeng", "pengwu");
        when(userService.createUser(Mockito.any(User.class))).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        User user = new User("2025123", "wupeng", "99988");
        when(userService.updateUser(Mockito.eq("2025123"), Mockito.any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/2025123")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("wupeng"));
    }

    @Test
    public void testDeleteUser() throws Exception {
        // Mock userService.deleteUser 返回 true
        when(userService.deleteUser("2025123")).thenReturn(true);

        mockMvc.perform(delete("/api/users/delete/2025123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).deleteUser("2025123");
    }
}