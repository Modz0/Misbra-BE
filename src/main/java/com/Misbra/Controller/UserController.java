package com.Misbra.Controller;

import com.Misbra.DTO.UserDTO;
import com.Misbra.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "API for all the user services")
public class UserController {

    private final UserService userService;

    /**
     * Constructs a new UserController with the given UserService.
     *
     * @param userService the service to manage user operations.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a list of all users.
     *
     * @return ResponseEntity containing the list of UserDTO objects and HTTP status OK.
     */
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users."
    )
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Retrieves a specific user by their unique ID.
     *
     * @param id the unique ID of the user.
     * @return ResponseEntity containing the UserDTO object and HTTP status OK.
     */
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by their unique ID."
    )
    @GetMapping("{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        UserDTO userDTO = userService.getUserById(id);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
}
