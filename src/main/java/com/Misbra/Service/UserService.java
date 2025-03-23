package com.Misbra.Service;

import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDTO> getAllUsers();
    UserDTO getUserById(String id);
    UserDTO getUserByEmail(String email);
    UserDTO addUser(UserDTO user);
    UserDTO updateUser(UserDTO user);
    Boolean existsByPhone(String phone);
    Optional<User> findByPhone(String phone);

}
