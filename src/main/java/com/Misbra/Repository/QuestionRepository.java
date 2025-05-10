package com.Misbra.Repository;

import com.Misbra.Entity.Question;
import com.Misbra.Enum.Difficulty;
import com.Misbra.Enum.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    /* single‑dimension filters */
    Page<Question> findByCategory(String category, Pageable pageable);
    /* utility */
    List<Question> findQuestionByCategory(String category);


}
