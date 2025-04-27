package com.Misbra.Service;

import com.Misbra.DTO.UserDTO;
import com.Misbra.Entity.User;
import com.Misbra.Enum.RoleEnum;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.UserMapper;
import com.Misbra.Repository.UserRepository;
import com.Misbra.Utils.AuthMessageKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        userRepository.save(userMapper.toEntity(user));

        return user;
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

    @Override
    public List<String> getAnsweredQuestions(String userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));

        // Return answered question IDs or an empty list if none found
        return Optional.ofNullable(user.getAnsweredQuestionIds()).orElse(new ArrayList<>());
    }

    /**
     * Records that a user has answered a specific question.
     *
     * @param userId     The user's unique identifier.
     * @param questionsId The question's unique identifier.
     */
    @Override
    public void addAnsweredQuestion(String userId, List<String> questionsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));


        // Use entity helper method for adding question
        questionsId.forEach(user::addAnsweredQuestion);

        // Save updated user
        userRepository.save(user);
    }

    @Override
    public void clearQuestionRecord (String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));

        user.setAnsweredQuestionIds(null);
        userRepository.save(user);
    }

    @Override
    public boolean isAdmin(String phone) {
        Optional<User> user = userRepository.findByPhone(phone);
        return user.map(u -> RoleEnum.ADMIN.equals(u.getRole()))
                .orElse(false);
    }
    @Override
   public void incrementGamesPlayed(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));

        user.setNumberOfGamesPlayed(user.getNumberOfGamesPlayed() + 1);
        userRepository.save(user);
    }
    @Override
    public Long getRemainingGames(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));
        return user.getNumberOfGamesRemaining();
    }
    @Override
    public void decreesGamesPlayed(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));

        user.setNumberOfGamesRemaining(user.getNumberOfGamesRemaining()-1);
        userRepository.save(user);
    }
    @Override
    public Long getFreeGames(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));
        return user.getNumberOfFreeGames();
    }

    @Override
    public void decreesFreeGamesPlayed(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " not found"));

        user.setNumberOfFreeGames(user.getNumberOfFreeGames()-1);
        userRepository.save(user);
    }



}
