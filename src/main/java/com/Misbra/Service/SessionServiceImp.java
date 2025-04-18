package com.Misbra.Service;

import com.Misbra.Component.SessionQuestions;
import com.Misbra.DTO.QuestionDTO;
import com.Misbra.DTO.SessionDTO;
import com.Misbra.Entity.Session;
import com.Misbra.Enum.*;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.SessionMapper;
import com.Misbra.Repository.SessionRepository;
import com.Misbra.Utils.BusinessMessageKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionServiceImp implements SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final ExceptionUtils exceptionUtils;
    private final QuestionService questionService;
    private final UserService userService;

    /**
     * Constructs a new SessionServiceImp with the required dependencies.
     *
     * @param sessionRepository Repository for session entities.
     * @param sessionMapper     Mapper to convert between Session and SessionDTO.
     * @param exceptionUtils    Utility for handling exceptions.
     */
    public SessionServiceImp(SessionRepository sessionRepository, SessionMapper sessionMapper, ExceptionUtils exceptionUtils, QuestionService questionService, UserService userService) {
        this.sessionRepository = sessionRepository;
        this.sessionMapper = sessionMapper;
        this.exceptionUtils = exceptionUtils;
        this.questionService = questionService;
        this.userService = userService;

    }

    @Override
    public List<SessionDTO> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        return sessions.stream()
                .map(sessionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDTO getSessionById(String id) {
        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session ID cannot be null or empty");
        }

        Optional<Session> sessionOptional = sessionRepository.findById(id);
        if (sessionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with ID: " + id);
        }

        return sessionMapper.toDTO(sessionOptional.get());
    }

    @Override
    public List<SessionDTO> getSessionsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null or empty");
        }

        List<Session> sessions = sessionRepository.findSessionsByUserId(userId);
        return sessions.stream()
                .map(sessionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDTO createSession(SessionDTO sessionDTO) {
        Session session = sessionMapper.toEntity(sessionDTO);
        Session savedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(savedSession);
    }

    @Override
    public SessionDTO updateSession(SessionDTO sessionDTO) {
        if (sessionDTO.getSessionId() == null || sessionDTO.getSessionId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session ID cannot be null or empty");
        }

        if (!sessionRepository.existsById(sessionDTO.getSessionId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with ID: " + sessionDTO.getSessionId());
        }

        Session session = sessionMapper.toEntity(sessionDTO);
        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(updatedSession);
    }

    @Override
    public SessionDTO addQuestionToSession(String sessionId, SessionQuestions sessionQuestion) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with ID: " + sessionId));

        String categoryId = sessionQuestion.getCategoryId();

        // Get or create the list for this category
        List<SessionQuestions> categoryQuestions = session.getCategoryQuestionsMap()
                .computeIfAbsent(categoryId, k -> new ArrayList<>());

        categoryQuestions.add(sessionQuestion);
        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(updatedSession);
    }


    @Override
    public SessionDTO updateTeamScore(String sessionId, int teamNumber, int points) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with ID: " + sessionId));

        if (teamNumber == 1) {
            session.setTeam1score(session.getTeam1score() + points);
        } else if (teamNumber == 2) {
            session.setTeam2score(session.getTeam2score() + points);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid team number: " + teamNumber);
        }

        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toDTO(updatedSession);
    }
    private Map<String, PowerupType> disableUsedPowerups(SessionDTO session) {
        Map<String, PowerupType> usedPowerupsByTeam = new HashMap<>();

        // Process team1 powerups
        session.getTeam1Powerups().stream()
                .filter(x -> x.isActive() && !x.isUsed())
                .forEach(x -> {
                    x.setUsed(true);
                    usedPowerupsByTeam.put(session.getTeam1name(), x.getType());
                });

        // Process team2 powerups
        session.getTeam2Powerups().stream()
                .filter(x -> x.isActive() && !x.isUsed())
                .forEach(x -> {
                    x.setUsed(true);
                    usedPowerupsByTeam.put(session.getTeam2name(), x.getType());
                });

        return usedPowerupsByTeam;
    }

    @Override
    public SessionDTO answerQuestion(String sessionId, String questionId, String teamId, SessionDTO session) {
        Map<String, PowerupType> usedPowerupsByTeam = disableUsedPowerups(session);
        Session sessionEntity = sessionMapper.toEntity(session);

        // Find the question in any category
        SessionQuestions foundQuestion = null;
        questionSearch:
        for (List<SessionQuestions> questions : sessionEntity.getCategoryQuestionsMap().values()) {
            for (SessionQuestions question : questions) {
                if (question.getQuestionId().equals(questionId)) {
                    foundQuestion = question;
                    break questionSearch; // Exit both loops when found
                }
            }
        }

        if (foundQuestion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found in session");
        }

        // Determine canonical team names
        String team1 = sessionEntity.getTeam1name();
        String team2 = sessionEntity.getTeam2name();
        boolean isTeam1 = teamId.equalsIgnoreCase(team1);
        boolean isTeam2 = teamId.equalsIgnoreCase(team2);

        // Process valid team answer
        if (isTeam1 || isTeam2) {
            foundQuestion.setAnswered(true);
            String canonicalTeamName = isTeam1 ? team1 : team2;
            foundQuestion.setAnsweredByTeam(canonicalTeamName);

            int basePoints = foundQuestion.getPointsAwarded();
            int pointsAwarded = basePoints;
            String opposingTeam = isTeam1 ? team2 : team1;

            // Handle answering team's powerups
            if (usedPowerupsByTeam.containsKey(canonicalTeamName)) {
                if (usedPowerupsByTeam.get(canonicalTeamName) == PowerupType.DOUBLE_OR_MINUS) {
                    pointsAwarded *= 2;
                }
            }

            // Handle opposing team's powerups
            if (usedPowerupsByTeam.containsKey(opposingTeam)) {
                PowerupType opposingPowerup = usedPowerupsByTeam.get(opposingTeam);
                if (opposingPowerup == PowerupType.MINUS_FIFTY_PERCENT) {
                    pointsAwarded = (int) Math.round(pointsAwarded * 0.5);
                }
            }

            // Update answering team's score
            if (isTeam1) {
                sessionEntity.setTeam1score(sessionEntity.getTeam1score() + pointsAwarded);
            } else {
                sessionEntity.setTeam2score(sessionEntity.getTeam2score() + pointsAwarded);
            }

            // Handle opposing team's DOUBLE_OR_MINUS penalty (only penalize, no bonus)
            if (usedPowerupsByTeam.containsKey(opposingTeam) &&
                    usedPowerupsByTeam.get(opposingTeam) == PowerupType.DOUBLE_OR_MINUS) {
                // Remove base points from opposing team
                if (opposingTeam.equals(team1)) {
                    sessionEntity.setTeam1score(sessionEntity.getTeam1score() - basePoints);
                } else {
                    sessionEntity.setTeam2score(sessionEntity.getTeam2score() - basePoints);
                }
                // No bonus points added to the answering team
            }
        } else {
            // No valid team answered
            foundQuestion.setAnswered(true);
            foundQuestion.setAnsweredByTeam("NO ONE");

            // Apply DOUBLE_OR_MINUS penalties for both teams
            if (usedPowerupsByTeam.containsKey(team1) &&
                    usedPowerupsByTeam.get(team1) == PowerupType.DOUBLE_OR_MINUS) {
                sessionEntity.setTeam1score(sessionEntity.getTeam1score() - foundQuestion.getPointsAwarded());
            }
            if (usedPowerupsByTeam.containsKey(team2) &&
                    usedPowerupsByTeam.get(team2) == PowerupType.DOUBLE_OR_MINUS) {
                sessionEntity.setTeam2score(sessionEntity.getTeam2score() - foundQuestion.getPointsAwarded());
            }
        }

        sessionEntity.setAnsweredQuestions(sessionEntity.getAnsweredQuestions() + 1);
        if (sessionEntity.getAnsweredQuestions() == sessionEntity.getTotalQuestions()) {
            sessionEntity.setSessionStatus(SessionStatus.COMPLETED);
        }

        // Save and return updated session
        Session updatedSession = sessionRepository.save(sessionEntity);
        return sessionMapper.toDTO(updatedSession);
    }

    @Override
    public boolean deleteSession(String id) {
        if (!sessionRepository.existsById(id)) {
            return false;
        }

        sessionRepository.deleteById(id);
        return true;
    }


    @Override
    public SessionDTO createSessionWithQuestions(String team1Id, String team2Id, String userId, List<String> categories, SessionType sessionType) {
        QuestionType sessionQuestionType=QuestionType.FREE;

        if(sessionType == SessionType.PAYED) {
            sessionQuestionType=QuestionType.PAYED;
        }

        // Create a new session with initial details
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setUserId(userId);
        sessionDTO.setTeam1name(team1Id);
        sessionDTO.setTeam2name(team2Id);
        sessionDTO.setTeam1score(0);
        sessionDTO.setTeam2score(0);
        sessionDTO.setGameCategories(categories);
        sessionDTO.setCategoryQuestionsMap(new HashMap<>());
        sessionDTO.setAnsweredQuestions(0);
        sessionDTO.setSessionStatus(SessionStatus.IN_PROGRESS);
        sessionDTO.setSessionQuestionType(sessionQuestionType);

        // Save the initial session to get an ID
        Session session = sessionMapper.toEntity(sessionDTO);
        session.initializePowerups();
        Session savedSession = sessionRepository.save(session);
        String sessionId = savedSession.getSessionId();
         int totalQuestions = 0;
        // For each category, get questions (2 easy, 4 medium, 2 hard)
        for (String category : categories) {
            List<String> allQuestionIds = new ArrayList<>();

            // Get easy questions (2)
            List<QuestionDTO> easyQuestions = questionService.getUnansweredQuestionsByCategory(userId, category, 2, Difficulty.EASY,sessionQuestionType);
            addQuestionsToSession(sessionId, easyQuestions, category, Difficulty.EASY);
            allQuestionIds.addAll(easyQuestions.stream().map(QuestionDTO::getQuestionId).toList());

            // Get medium questions (4)
            List<QuestionDTO> mediumQuestions = questionService.getUnansweredQuestionsByCategory(userId, category, 4, Difficulty.MEDIUM,sessionQuestionType);
            addQuestionsToSession(sessionId, mediumQuestions, category, Difficulty.MEDIUM);
            allQuestionIds.addAll(mediumQuestions.stream().map(QuestionDTO::getQuestionId).toList());

            // Get hard questions (2)
            List<QuestionDTO> hardQuestions = questionService.getUnansweredQuestionsByCategory(userId, category, 2, Difficulty.HARD,sessionQuestionType);
            addQuestionsToSession(sessionId, hardQuestions, category, Difficulty.HARD);
            allQuestionIds.addAll(hardQuestions.stream().map(QuestionDTO::getQuestionId).toList());

            log.info("Collected question IDs for category {}: {}", category, allQuestionIds);
            totalQuestions+=allQuestionIds.size();
            // Add these questions to the user's answered questions list
            userService.addAnsweredQuestion(userId, allQuestionIds);
        }
        userService.incrementGamesPlayed(userId);


        if(sessionType == SessionType.PAYED) {
            userService.decreesGamesPlayed(userId);
        }else
        {
            userService.decreesFreeGamesPlayed(userId);
        }


        // Get the complete session with all added questions
        return getSessionById(sessionId);
    }



    private void addQuestionsToSession(String sessionId, List<QuestionDTO> questions, String categoryId, Difficulty difficulty) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        // Get or create the list for this category
        List<SessionQuestions> categoryQuestions = session.getCategoryQuestionsMap()
                .computeIfAbsent(categoryId, k -> new ArrayList<>());

        for (QuestionDTO question : questions) {
            // Check if this question is already added to this category to prevent duplicates
            boolean isDuplicate = categoryQuestions.stream()
                    .anyMatch(sq -> sq.getQuestionId().equals(question.getQuestionId()) &&
                            sq.getDifficulty().equals(difficulty));

            // Only add if not a duplicate
            if (!isDuplicate) {
                SessionQuestions sessionQuestion = new SessionQuestions();
                sessionQuestion.setQuestionId(question.getQuestionId());
                sessionQuestion.setCategoryId(categoryId);
                sessionQuestion.setAnswered(false);
                sessionQuestion.setAnsweredByTeam(null);
                sessionQuestion.setDifficulty(difficulty);

                // Set points based on difficulty
                if (difficulty.equals(Difficulty.EASY)) {
                    sessionQuestion.setPointsAwarded(250);
                } else if (difficulty.equals(Difficulty.MEDIUM)) {
                    sessionQuestion.setPointsAwarded(500);
                } else if (difficulty.equals(Difficulty.HARD)) {
                    sessionQuestion.setPointsAwarded(1000);
                }

                // Add the question to the category-specific list
                categoryQuestions.add(sessionQuestion);
                session.setTotalQuestions(session.getTotalQuestions() + 1);
            }
        }

        sessionRepository.save(session);
    }







}