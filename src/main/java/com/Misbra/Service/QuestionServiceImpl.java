package com.Misbra.Service;

import com.Misbra.DTO.QuestionDTO;
import com.Misbra.Entity.Question;
import com.Misbra.Mapper.QuestionMapper;
import com.Misbra.Repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public QuestionServiceImpl(
            QuestionRepository questionRepository,
            QuestionMapper questionMapper,
            UserService userService,
            MongoTemplate mongoTemplate) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<QuestionDTO> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        return questions.stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionDTO getQuestionById(String id) {
        Optional<Question> question = questionRepository.findById(id);
        return question.map(questionMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question with ID " + id + " not found"));
    }

    @Override
    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        Question question = questionMapper.toEntity(questionDTO);
        Question savedQuestion = questionRepository.save(question);
        return questionMapper.toDTO(savedQuestion);
    }

    @Override
    public QuestionDTO updateQuestion(QuestionDTO questionDTO) {
        if (!questionRepository.existsById(questionDTO.getQuestionId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Question with ID " + questionDTO.getQuestionId() + " not found");
        }

        Question question = questionMapper.toEntity(questionDTO);
        Question updatedQuestion = questionRepository.save(question);
        return questionMapper.toDTO(updatedQuestion);
    }

    @Override
    public void deleteQuestion(String id) {
        if (!questionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Question with ID " + id + " not found");
        }
        questionRepository.deleteById(id);
    }

    @Override
    public Page<QuestionDTO> getQuestionsByCategory(String category, Pageable pageable) {
        Page<Question> questions = questionRepository.findByCategory(category ,pageable );
        return questions.map(questionMapper::toDTO);
    }


    @Override
    public List<QuestionDTO> getUnansweredQuestionsByCategory(String userId, String category, int limit, String difficulty) {
        // Get list of questions the user has already answered
        List<String> answeredQuestionIds = userService.getAnsweredQuestions(userId);

        // Create query to find questions not in the answered list
        Query query = new Query();
        if (!answeredQuestionIds.isEmpty()) {
            query.addCriteria(Criteria.where("questionId").nin(answeredQuestionIds));
        }

        // Add category and difficulty criteria
        query.addCriteria(Criteria.where("category").is(category));
        query.addCriteria(Criteria.where("difficulty").is(difficulty));

        // Add random sorting and limit
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.limit(limit);

        List<Question> questions = mongoTemplate.find(query, Question.class);

        // If we don't have enough questions, get some from the already answered ones
        if (questions.size() < limit) {
            int remainingQuestions = limit - questions.size();

            Query answeredQuery = new Query();
            answeredQuery.addCriteria(Criteria.where("category").is(category));
            answeredQuery.addCriteria(Criteria.where("difficulty").is(difficulty));

            if (!answeredQuestionIds.isEmpty()) {
                answeredQuery.addCriteria(Criteria.where("questionId").in(answeredQuestionIds));
            }

            answeredQuery.with(Sort.by(Sort.Direction.DESC, "createdAt"));
            answeredQuery.limit(remainingQuestions);

            List<Question> answeredQuestions = mongoTemplate.find(answeredQuery, Question.class);
            questions.addAll(answeredQuestions);
        }

        return questions.stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());
    }


}