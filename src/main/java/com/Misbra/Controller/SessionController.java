package com.Misbra.Controller;

import com.Misbra.Component.PowerupsRequest;
import com.Misbra.Component.SessionQuestions;
import com.Misbra.Component.TeamPowerup;
import com.Misbra.DTO.SessionDTO;
import com.Misbra.Entity.User;
import com.Misbra.Service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Retrieves all sessions.
     *
     * @return ResponseEntity containing a list of all sessions
     */
    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    /**
     * Retrieves a session by its ID.
     *
     * @param id the ID of the session to retrieve
     * @return ResponseEntity containing the session
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable String id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    /**
     * Retrieves sessions by user ID.
     *
     * @return ResponseEntity containing a list of the user's sessions
     */
    @GetMapping("/user")
    public ResponseEntity<List<SessionDTO>> getSessionsByUserId(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(sessionService.getSessionsByUserId(currentUser.getUserId()));
    }

    /**
     * Creates a new session.
     *
     * @param sessionDTO the session to create
     * @return ResponseEntity containing the created session
     */
    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@RequestBody SessionDTO sessionDTO) {
        return new ResponseEntity<>(sessionService.createSession(sessionDTO), HttpStatus.CREATED);
    }

    /**
     * Creates a new session with questions for the specified categories.
     * For each category, it includes 2 easy, 4 medium, and 2 hard questions.
     *
     * @param currentUser the authenticated user creating the session

     * @return ResponseEntity containing the created session with questions
     */
    @PostMapping("/with-questions")
    public ResponseEntity<SessionDTO> createSessionWithQuestions(
            @AuthenticationPrincipal User currentUser,
            @RequestBody SessionDTO session) {
        return new ResponseEntity<>(
                sessionService.createSessionWithQuestions(session.getTeam1name(), session.getTeam2name(), currentUser.getUserId(), session.getGameCategories(),session.getSessionType()),
                HttpStatus.CREATED
        );
    }

    /**
     * Updates an existing session.
     *
     * @param id the ID of the session to update
     * @param sessionDTO the updated session details
     * @return ResponseEntity containing the updated session
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<SessionDTO> updateSession(@PathVariable String id, @RequestBody SessionDTO sessionDTO) {
        log.info(sessionDTO.toString());
        return ResponseEntity.ok(sessionService.updateSession(sessionDTO));
    }

    /**
     * Adds a question to a session.
     *
     * @param sessionId the ID of the session
     * @param sessionQuestion the question to add
     * @return ResponseEntity containing the updated session
     */
    @PostMapping("/{sessionId}/questions")
    public ResponseEntity<SessionDTO> addQuestionToSession(
            @PathVariable String sessionId,
            @RequestBody SessionQuestions sessionQuestion) {
        return ResponseEntity.ok(sessionService.addQuestionToSession(sessionId, sessionQuestion));
    }

    /**
     * Updates a team's score for a session.
     *
     * @param sessionId the ID of the session
     * @param teamNumber the team number (1 or 2)
     * @param points the points to add
     * @return ResponseEntity containing the updated session
     */
    @PutMapping("/{sessionId}/score")
    public ResponseEntity<SessionDTO> updateTeamScore(
            @PathVariable String sessionId,
            @RequestParam int teamNumber,
            @RequestParam int points) {
        return ResponseEntity.ok(sessionService.updateTeamScore(sessionId, teamNumber, points));
    }

    /**
     * Records a question answer in a session.
     *
     * @param sessionId the ID of the session
     * @param questionId the ID of the question
     * @param teamId the ID of the answering team

     * @return ResponseEntity containing the updated session
     */
    @PutMapping("/{sessionId}/question/{questionId}/answer")
    public ResponseEntity<SessionDTO> answerQuestion(
            @PathVariable String sessionId,
            @PathVariable String questionId,
            @RequestParam String teamId,
             @RequestBody PowerupsRequest teamsPowerUps

    ) {
        return ResponseEntity.ok(sessionService.answerQuestion(sessionId, questionId, teamId,teamsPowerUps.getTeam1Powerups() ,teamsPowerUps.getTeam2Powerups()));
    }

    /**
     * Deletes a session.
     *
     * @param id the ID of the session to delete
     * @return ResponseEntity with no content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        if (sessionService.deleteSession(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}