package com.Misbra.ConrollerTest;

import com.Misbra.Controller.UserController;
import com.Misbra.DTO.UserDTO;
import com.Misbra.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(new UserDTO()));
        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetUserById() {
        UserDTO dto = new UserDTO();
        when(userService.getUserById("1")).thenReturn(dto);
        ResponseEntity<UserDTO> response = userController.getUser("1");
        assertNotNull(response.getBody());
    }
}
