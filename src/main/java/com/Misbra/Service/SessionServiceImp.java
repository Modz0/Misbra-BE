package com.Misbra.Service;

import com.Misbra.Component.SessionQuestions;
import com.Misbra.DTO.QuestionDTO;
import com.Misbra.DTO.SessionDTO;
import com.Misbra.Entity.Session;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Mapper.SessionMapper;
import com.Misbra.Repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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
     * @param sessionMapper Mapper to convert between Session and SessionDTO.
     * @param exceptionUtils Utility for handling exceptions.
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

        session.getGameQuestions().add(sessionQuestion);
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

    @Override
    public SessionDTO answerQuestion(String sessionId, String questionId, String teamId, boolean correct, int pointsAwarded) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with ID: " + sessionId));

        // Find the question in the session
        Optional<SessionQuestions> questionOptional = session.getGameQuestions().stream()
                .filter(q -> q.getQuestionId().equals(questionId))
                .findFirst();

        if (questionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found in session");
        }

        SessionQuestions question = questionOptional.get();
        question.setAnswered(true);
        question.setAnsweredByTeam(teamId);
        question.setAnsweredCorrectly(correct);
        question.setPointsAwarded(pointsAwarded);

        // Update team score if answer was correct
        if (correct) {
            if (teamId.equals(session.getTeam1Id())) {
                session.setTeam1score(session.getTeam1score() + pointsAwarded);
            } else if (teamId.equals(session.getTeam2Id())) {
                session.setTeam2score(session.getTeam2score() + pointsAwarded);
            }
        }

        Session updatedSession = sessionRepository.save(session);
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
    public SessionDTO createSessionWithQuestions(String team1Id, String team2Id, String userId, List<String> categories) {
        // Create a new session with initial details
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setUserId(userId);
        sessionDTO.setTeam1Id(team1Id);
        sessionDTO.setTeam2Id(team2Id);
        sessionDTO.setTeam1score(0);
        sessionDTO.setTeam2score(0);
        sessionDTO.setGameCategories(categories);

        // Save the initial session to get an ID
        Session session = sessionMapper.toEntity(sessionDTO);
        Session savedSession = sessionRepository.save(session);
        String sessionId = savedSession.getSessionId();

        // For each category, get 8 questions (2 easy, 4 medium, 2 hard)
        for (String category : categories) {
            // Get easy questions (2)
            List<QuestionDTO> easyQuestions = questionService.getUnansweredQuestionsByCategory(userId, category, 2, "EASY");
            addQuestionsToSession(sessionId, easyQuestions, category);

            // Get medium questions (4)
            List<QuestionDTO> mediumQuestions = questionService.getUnansweredQuestionsByCategory(userId, category, 4, "MEDIUM");
            addQuestionsToSession(sessionId, mediumQuestions, category);

            // Get hard questions (2)
            List<QuestionDTO> hardQuestions = questionService.getUnansweredQuestionsByCategory(userId, category, 2, "HARD");
            addQuestionsToSession(sessionId, hardQuestions, category);

            // Add all questions to user's answered questions list
            List<String> allQuestionIds = new ArrayList<>();
            allQuestionIds.addAll(easyQuestions.stream().map(QuestionDTO::getQuestionId).toList());
            allQuestionIds.addAll(mediumQuestions.stream().map(QuestionDTO::getQuestionId).toList());
            allQuestionIds.addAll(hardQuestions.stream().map(QuestionDTO::getQuestionId).toList());

            // Add these questions to the user's answered questions list
            userService.addAnsweredQuestion(userId, allQuestionIds);
        }

        // Get the complete session with all added questions
        return getSessionById(sessionId);
    }


    private void addQuestionsToSession(String sessionId, List<QuestionDTO> questions, String categoryId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        for (QuestionDTO question : questions) {
            SessionQuestions sessionQuestion = new SessionQuestions();
            sessionQuestion.setQuestionId(question.getQuestionId());
            sessionQuestion.setCategoryId(categoryId);
            sessionQuestion.setAnswered(false);
            sessionQuestion.setAnsweredByTeam(null);
            sessionQuestion.setAnsweredCorrectly(false);
            sessionQuestion.setPointsAwarded(0);

            session.getGameQuestions().add(sessionQuestion);
        }

        sessionRepository.save(session);
    }




}