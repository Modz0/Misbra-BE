package com.Misbra.Service;

import com.Misbra.Component.SessionQuestions;
import com.Misbra.Component.TeamPowerup;
import com.Misbra.DTO.SessionDTO;
import com.Misbra.Entity.Session;

import java.util.List;

public interface SessionService {

    /**
     * Retrieves all sessions.
     *
     * @return a List of SessionDTO objects representing all sessions
     */
    List<SessionDTO> getAllSessions();

    /**
     * Retrieves a session by its ID.
     *
     * @param id the ID of the session
     * @return a SessionDTO representing the found session
     */
    SessionDTO getSessionById(String id);

    /**
     * Retrieves all sessions for a specific user.
     *
     * @param userId the ID of the user
     * @return a List of SessionDTO objects belonging to the user
     */
    List<SessionDTO> getSessionsByUserId(String userId);

    /**
     * Creates a new session.
     *
     * @param sessionDTO the SessionDTO containing the new session details
     * @return the created SessionDTO with generated ID
     */
    SessionDTO createSession(SessionDTO sessionDTO);

    /**
     * Updates an existing session.
     *
     * @param sessionDTO the SessionDTO containing updated session details
     * @return the updated SessionDTO
     */
    SessionDTO updateSession(SessionDTO sessionDTO);

    /**
     * Adds a question to a session.
     *
     * @param sessionId the ID of the session
     * @param sessionQuestion the question to add to the session
     * @return the updated SessionDTO
     */
    SessionDTO addQuestionToSession(String sessionId, SessionQuestions sessionQuestion);

    /**
     * Updates the team score for a session.
     *
     * @param sessionId the ID of the session
     * @param teamNumber the team number (1 or 2)
     * @param points the points to add to the team's score
     * @return the updated SessionDTO
     */
    SessionDTO updateTeamScore(String sessionId, int teamNumber, int points);

    /**
     * Marks a question as answered in a session.
     *
     * @param sessionId the ID of the session
     * @param questionId the ID of the question
     * @param teamId the ID of the team that answered
     * @return the updated SessionDTO
     */

    SessionDTO answerQuestion(String sessionId, String questionId, String teamId ,SessionDTO session);


    /**
     * Creates a session with questions for the specified categories,
     * following the distribution of 2 easy, 4 medium, and 2 hard questions
     * per category.
     *
     * @param team1Id ID of the first team
     * @param team2Id ID of the second team
     * @param userId ID of the user creating the session
     * @param categories List of category names to include
     * @return the created session with questions
     */
    SessionDTO createSessionWithQuestions(String team1Id, String team2Id, String userId, List<String> categories);



    /**
     * Deletes a session.
     *
     * @param id the ID of the session to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteSession(String id);
}