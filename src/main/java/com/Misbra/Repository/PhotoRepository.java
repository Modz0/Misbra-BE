package com.Misbra.Repository;

import com.Misbra.Entity.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends MongoRepository<Photo,String> {

    List<Photo> findAllByPhotoIdIn(List<String> photoIds);

    List<Photo> findByReviewId(String reviewId);
}
