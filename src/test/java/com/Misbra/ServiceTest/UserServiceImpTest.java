package com.Misbra.ServiceTest;


import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.User;
import com.Misbra.Enum.RoleEnum;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Mapper.UserMapper;
import com.Misbra.Repository.UserRepository;
import com.Misbra.Service.UserServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ExceptionUtils exceptionUtils;

    @InjectMocks
    private UserServiceImp userServiceImp;

    @Test
    void testGetAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());
        List<UserDTO> users = userServiceImp.getAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetAllUsers_NonEmpty() {
        User entity = new User();
        UserDTO dto = new UserDTO();
        when(userRepository.findAll()).thenReturn(List.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(dto);
        List<UserDTO> users = userServiceImp.getAllUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    void testGetUserByEmail_HappyPath() {
        User entity = new User();
        UserDTO dto = new UserDTO();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(dto);
        UserDTO result = userServiceImp.getUserByEmail("test@example.com");
        assertNotNull(result);
    }

    @Test
    void testGetUserByEmail_NullEmail() {
        assertThrows(ResponseStatusException.class, () -> userServiceImp.getUserByEmail(null));
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> userServiceImp.getUserByEmail("notfound@example.com"));
    }

    @Test
    void testAddUser_HappyPath() {
        UserDTO dto = new UserDTO();
        dto.setEmail("test@example.com");
        dto.setPhone("1234567890");
        when(userRepository.existsByPhone("test@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(new User());
        UserDTO result = userServiceImp.addUser(dto);
        assertEquals(dto, result);
    }

    @Test
    void testAddUser_AlreadyExists() {
        UserDTO dto = new UserDTO();
        dto.setEmail("test@example.com");
        dto.setPhone("1234567890");
        // Stub with phone.
        when(userRepository.existsByPhone("1234567890")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> userServiceImp.addUser(dto));
    }

    @Test
    void testUpdateUser_HappyPath() {
        User existing = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existing));
        existing.setEmail("test@example.com");
        existing.setPhone("1234567890");
        UserDTO dto = new UserDTO();
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPhone("1234567890");
        dto.setEnabled(true);
        dto.setRole(RoleEnum.USER);
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDTO(existing)).thenReturn(dto);
        UserDTO result = userServiceImp.updateUser(dto);
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void testExistsByPhone() {
        when(userRepository.existsByPhone("1234567890")).thenReturn(true);
        assertTrue(userServiceImp.existsByPhone("1234567890"));
    }

    @Test
    void testFindByPhone() {
        User entity = new User();
        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.of(entity));
        Optional<User> result = userServiceImp.findByPhone("1234567890");
        assertTrue(result.isPresent());
    }

    @Test
    void testGetUserById_HappyPath() {
        User entity = new User();
        when(userRepository.findById("1")).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(new UserDTO());
        UserDTO result = userServiceImp.getUserById("1");
        assertNotNull(result);
    }

    @Test
    void testGetUserById_InvalidId() {
        assertThrows(ResponseStatusException.class, () -> userServiceImp.getUserById("0"));
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> userServiceImp.getUserById("2"));
    }
}
