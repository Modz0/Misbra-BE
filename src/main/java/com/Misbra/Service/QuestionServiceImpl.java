package com.Misbra.Service;

import com.Misbra.DTO.CategoryDTO;
import com.Misbra.DTO.PhotoDTO;
import com.Misbra.DTO.QuestionDTO;
import com.Misbra.Entity.Category;
import com.Misbra.Entity.Question;
import com.Misbra.Enum.Difficulty;
import com.Misbra.Enum.QuestionType;
import com.Misbra.Enum.referenceType;
import com.Misbra.Mapper.QuestionMapper;
import com.Misbra.Repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final PhotoService photoService;

    @Autowired
    public QuestionServiceImpl(
            QuestionRepository questionRepository,
            QuestionMapper questionMapper,
            UserService userService,
            MongoTemplate mongoTemplate, PhotoService photoService) {
        this.questionRepository = questionRepository;
        this.questionMapper = questionMapper;
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
        this.photoService = photoService;
    }
    @Override
    public Page<QuestionDTO> getAllQuestions(Pageable pageable,
                                             List<String>       selectedCategory,
                                             List<Difficulty>   selectedDifficulty,
                                             List<QuestionType> selectedQuestionType) {

        /* ---------- build the query dynamically ---------- */
        Query query = new Query();

        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            query.addCriteria(Criteria.where("category").in(selectedCategory));
        }

        if (selectedDifficulty != null && !selectedDifficulty.isEmpty()) {
            query.addCriteria(Criteria.where("difficulty").in(selectedDifficulty));
        }

        if (selectedQuestionType != null && !selectedQuestionType.isEmpty()) {
            query.addCriteria(Criteria.where("questionType").in(selectedQuestionType));
        }

        long total = mongoTemplate.count(query, Question.class);   // total before pagination
        query.with(pageable);                                      // apply limit / skip

        List<Question> questions = mongoTemplate.find(query, Question.class);

        /* ---------- map to DTOs & enrich with thumbnails (unchanged) ---------- */
        List<String> allPhotoIds = Stream.concat(
                        questions.stream().map(Question::getQuestionPhotoId),
                        questions.stream().map(Question::getAnswerPhotoId))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<QuestionDTO> dtoList = questions.stream()
                .map(questionMapper::toDTO)
                .toList();

        if (!allPhotoIds.isEmpty()) {
            Map<String, String> urls = photoService.getBulkPresignedUrls(allPhotoIds);
            for (int i = 0; i < questions.size(); i++) {
                Question     q  = questions.get(i);
                QuestionDTO  d  = dtoList.get(i);
                if (q.getQuestionPhotoId() != null)
                    d.setQuestionThumbnailUrl(urls.get(q.getQuestionPhotoId()));
                if (q.getAnswerPhotoId() != null)
                    d.setAnswerThumbnailUrl(urls.get(q.getAnswerPhotoId()));
            }
        }

        return new PageImpl<>(dtoList, pageable, total);
    }




    @Override
    public QuestionDTO getQuestionById(String id) {
        Optional<Question> questionOptional = questionRepository.findById(id);

        // Check if question exists and throw exception if not
        if (questionOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Question with ID " +id + " not found");        }

        Question question = questionOptional.get();
        String questionPhotoId = question.getQuestionPhotoId();
        String answerPhotoId = question.getAnswerPhotoId();
        QuestionDTO questionDTO = questionMapper.toDTO(question);

        if (questionPhotoId != null) {
            String questionPreSignedUrl = photoService.getPresignedUrl(questionPhotoId);
            questionDTO.setQuestionThumbnailUrl(questionPreSignedUrl);
        }

        if (answerPhotoId != null) {
            String answerPreSignedUrl = photoService.getPresignedUrl(answerPhotoId);
            questionDTO.setAnswerThumbnailUrl(answerPreSignedUrl);
        }

        return questionDTO;
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
        Question question = questionRepository.findById(id).orElse(null);
        if(ObjectUtils.isEmpty(question)){
            throw new RuntimeException("Category not found with id: " + id);
        }
        if (!ObjectUtils.isEmpty(question.getQuestionPhotoId())){
            photoService.deletePhotoById(question.getQuestionPhotoId());
        }



        if (!ObjectUtils.isEmpty(question.getAnswerPhotoId())){
            photoService.deletePhotoById(question.getAnswerPhotoId());
        }

        questionRepository.deleteById(id);
    }
    @Override
    public Page<QuestionDTO> getQuestionsByCategory(String category, Pageable pageable) {
        Page<Question> questions = questionRepository.findByCategory(category, pageable);

        // Gather all question and answer thumbnail IDs from this page only
        List<String> questionPhotoIds = questions.getContent().stream()
                .map(Question::getQuestionPhotoId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> answerPhotoIds = questions.getContent().stream()
                .map(Question::getAnswerPhotoId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Combine all photo IDs that need presigned URLs
        List<String> allPhotoIds = new ArrayList<>();
        allPhotoIds.addAll(questionPhotoIds);
        allPhotoIds.addAll(answerPhotoIds);

        // Get presigned URLs for all thumbnails in this page
        if (!allPhotoIds.isEmpty()) {
            Map<String, String> presignedUrls = photoService.getBulkPresignedUrls(allPhotoIds);

            // Apply the presigned URLs to the DTOs during mapping
            return questions.map(question -> {
                QuestionDTO dto = questionMapper.toDTO(question);

                // Set question thumbnail URL
                if (question.getQuestionPhotoId() != null &&
                        presignedUrls.containsKey(question.getQuestionPhotoId())) {
                    dto.setQuestionThumbnailUrl(presignedUrls.get(question.getQuestionPhotoId()));
                }

                // Set answer thumbnail URL
                if (question.getAnswerPhotoId() != null &&
                        presignedUrls.containsKey(question.getAnswerPhotoId())) {
                    dto.setAnswerThumbnailUrl(presignedUrls.get(question.getAnswerPhotoId()));
                }

                return dto;
            });
        }

        return questions.map(questionMapper::toDTO);
    }

    @Override
    public List<QuestionDTO> getUnansweredQuestionsByCategory(String userId, String category, int limit, Difficulty difficulty, QuestionType questionType) {
        // Get list of questions the user has already answered
        List<String> answeredQuestionIds = userService.getAnsweredQuestions(userId);

        // Create criteria for the query
        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>();

        // Add filter for answered questions
        if (!answeredQuestionIds.isEmpty()) {
            criteriaList.add(Criteria.where("questionId").nin(answeredQuestionIds));
        }

        // Add category and difficulty criteria
        criteriaList.add(Criteria.where("category").is(category));
        criteriaList.add(Criteria.where("difficulty").is(difficulty));

        // Add question type criteria
        if(questionType.equals(QuestionType.PAYED)){
            criteriaList.add(Criteria.where("questionType").in(questionType));
        } else {
            criteriaList.add(Criteria.where("questionType").is(questionType));
        }

        // Combine all criteria
        if (!criteriaList.isEmpty()) {
            criteria = criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        }

        // Use aggregation for random selection
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sample(limit)
        );

        List<Question> questions = mongoTemplate.aggregate(
                aggregation, "questions", Question.class).getMappedResults();

        // Gather all photo IDs (both question and answer)
        List<String> questionPhotoIds = questions.stream()
                .map(Question::getQuestionPhotoId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> answerPhotoIds = questions.stream()
                .map(Question::getAnswerPhotoId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Combine all photo IDs
        List<String> allPhotoIds = new ArrayList<>();
        allPhotoIds.addAll(questionPhotoIds);
        allPhotoIds.addAll(answerPhotoIds);

        // Convert questions to DTOs
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(questionMapper::toDTO)
                .collect(Collectors.toList());

        // Get presigned URLs for all thumbnails
        if (!allPhotoIds.isEmpty()) {
            Map<String, String> presignedUrls = photoService.getBulkPresignedUrls(allPhotoIds);

            for (QuestionDTO dto : questionDTOs) {
                // Set question thumbnail URL
                if (dto.getQuestionPhotoId() != null &&
                        presignedUrls.containsKey(dto.getQuestionPhotoId())) {
                    dto.setQuestionThumbnailUrl(presignedUrls.get(dto.getQuestionPhotoId()));
                }

                // Set answer thumbnail URL
                if (dto.getAnswerPhotoId() != null &&
                        presignedUrls.containsKey(dto.getAnswerPhotoId())) {
                    dto.setAnswerThumbnailUrl(presignedUrls.get(dto.getAnswerPhotoId()));
                }
            }
        }

        return questionDTOs;
    }

    @Override
    public int calculateAvailableGamesCount(String categoryId, String userId , QuestionType questionType) {
        List<String> answeredQuestionIds = userService.getAnsweredQuestions(userId);

        // Create query to find questions not in the answered list
        Query query = new Query();
        if (!answeredQuestionIds.isEmpty()) {
            query.addCriteria(Criteria.where("questionId").nin(answeredQuestionIds));
        }

        // Add category criteria
        query.addCriteria(Criteria.where("category").is(categoryId));


        query.addCriteria(Criteria.where("questionType").is(questionType));


        // Add sorting by creation date
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Question> questions = mongoTemplate.find(query, Question.class);

        // Count questions by difficulty
        long easyQuestionCount = questions.stream().filter(x -> x.getDifficulty().equals(Difficulty.EASY)).count();
        long mediumQuestionCount = questions.stream().filter(x -> x.getDifficulty().equals(Difficulty.MEDIUM)).count();
        long hardQuestionCount = questions.stream().filter(x -> x.getDifficulty().equals(Difficulty.HARD)).count();

        // Calculate possible games from each difficulty
        long possibleGamesFromEasy = easyQuestionCount / 2;
        long possibleGamesFromMedium = mediumQuestionCount / 4;
        long possibleGamesFromHard = hardQuestionCount / 2;

        // Return the minimum number of possible games (the limiting factor)
        return (int) Math.min(possibleGamesFromEasy, Math.min(possibleGamesFromMedium, possibleGamesFromHard));
    }

    @Override
    public void clearQuestionRecord(String userId){
        userService.clearQuestionRecord(userId);
    }


    @Override
    public String uploadQuestionPhotos(String questionId, MultipartFile file) throws IOException {
        PhotoDTO newPhoto = photoService.uploadPhoto(referenceType.QUESTION, questionId, file);
        Question question = questionRepository.findById(questionId).orElse(null);
        if(ObjectUtils.isEmpty(question)){
            throw new RuntimeException("Category not found with id: " + questionId);
        }
        if (!ObjectUtils.isEmpty(question.getQuestionPhotoId())){
            photoService.deletePhotoById(question.getQuestionPhotoId());
        }


        setQuestionThumbnail(questionId, newPhoto.getPhotoId());
        return photoService.getPresignedUrl(newPhoto.getPhotoId());
    }

    @Override
    public String uploadAnswerPhotos(String questionId, MultipartFile file) throws IOException {
        PhotoDTO newPhoto = photoService.uploadPhoto(referenceType.ANSWER, questionId, file);
        Question question = questionRepository.findById(questionId).orElse(null);

        if(ObjectUtils.isEmpty(question)){
            throw new RuntimeException("Category not found with id: " + questionId);
        }

        if (!ObjectUtils.isEmpty(question.getAnswerPhotoId())){
            photoService.deletePhotoById(question.getAnswerPhotoId());
        }
        setAnswerThumbnail(questionId, newPhoto.getPhotoId());
        return photoService.getPresignedUrl(newPhoto.getPhotoId());
    }
    @Override
    public void setQuestionThumbnail(String questionId, String photoId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + questionId));

        question.setQuestionPhotoId(photoId);
        questionRepository.save(question);

    }
    @Override
    public void setAnswerThumbnail(String questionId, String photoId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + questionId));

        question.setAnswerPhotoId(photoId);
        questionRepository.save(question);

    }
     @Override
     public List<QuestionDTO> findQuestionByCategory (String category) {
        List<Question> questions = questionRepository.findQuestionByCategory(category);

        return questions.stream().map(questionMapper::toDTO).collect(Collectors.toList());
     }


}