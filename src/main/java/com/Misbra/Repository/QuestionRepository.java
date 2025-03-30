package com.Misbra.Repository;

import com.Misbra.Entity.Question;
import com.Misbra.Enum.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    Page<Question> findByCategory(String category , Pageable pageable);
    Page<Question> findByDifficultyIn(Pageable pageable,List<Difficulty> difficulty);
    Page<Question> findByCategoryIn(Pageable pageable,List<String> categories);
    Page<Question> findByCategoryInAndDifficultyIn(Pageable pageable,List<String> categories, List<Difficulty> difficulty);
    List<Question> findByIsAnswered(boolean isAnswered);
}