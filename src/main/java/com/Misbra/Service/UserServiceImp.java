package com.Misbra.Service;

import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.User;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.UserMapper;
import com.Misbra.Repository.UserRepository;
import com.Misbra.Utils.AuthMessageKeys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {

    final UserRepository userRepository;
    final UserMapper userMapper;
    final ExceptionUtils exceptionUtils;

    /**
     * Constructs a new UserServiceImp with the required dependencies.
     *
     * @param userRepository Repository for user entities.
     * @param userMapper Mapper to convert between User and UserDTO.
     * @param exceptionUtils Utility for handling exceptions.
     */
    public UserServiceImp(UserRepository userRepository, UserMapper userMapper, ExceptionUtils exceptionUtils) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.exceptionUtils = exceptionUtils;
    }

    /**
     * Retrieves all users.
     *
     * @return a List of UserDTO objects representing all users; returns an empty list if no users are found.
     */
    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.isEmpty() ? List.of() : users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the email address of the user.
     * @return a UserDTO representing the found user.
     * @throws ResponseStatusException if the email is null or if no user is found with the given email.
     */
    @Override
    public UserDTO getUserByEmail(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserDTO cannot be null");
        }
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return userMapper.toDTO(user.get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    /**
     * Adds a new user.
     *
     * @param user the UserDTO containing the new user's details.
     * @return the same UserDTO that was added.
     */
    @Override
    public UserDTO addUser(UserDTO user) {
        if (userRepository.existsByPhone(user.getEmail())) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.USER_ALREADY_REGISTERED,
                    new String[]{user.getPhone()}
            ));
            exceptionUtils.throwValidationException(errors);
        }
        userRepository.save(userMapper.toEntity(user));
        return user;
    }

    /**
     * Updates an existing user.
     *
     * @param user the UserDTO containing updated user details.
     * @return the updated UserDTO.
     */
    @Override
    public UserDTO updateUser(UserDTO user) {
        User existingUser = userRepository.findByEmail(user.getEmail())
                .orElseGet(() -> {
                    List<ValidationErrorDTO> errors = new ArrayList<>();
                    errors.add(new ValidationErrorDTO(
                            AuthMessageKeys.USER_NOT_FOUND,
                            new String[]{user.getPhone()}
                    ));
                    exceptionUtils.throwValidationException(errors);
                    return null;
                });
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        existingUser.setEnabled(user.isEnabled());
        existingUser.setRole(user.getRole());
        userRepository.save(existingUser);
        return userMapper.toDTO(existingUser);
    }

    /**
     * Checks if a user exists by phone number.
     *
     * @param phone the phone number to check.
     * @return true if a user exists with the given phone, false otherwise.
     */
    @Override
    public Boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    /**
     * Finds a user by phone number.
     *
     * @param phone the phone number to search for.
     * @return an Optional containing the User if found, or empty if not found.
     */
    @Override
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user.
     * @return a UserDTO representing the found user.
     * @throws ResponseStatusException if the provided ID is invalid or no user is found with the given ID.
     */
    @Override
    public UserDTO getUserById(String id) {
        // Validate input
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid place ID");
        }
        User User = userRepository.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id " + id));
        return userMapper.toDTO(User);
    }
}
