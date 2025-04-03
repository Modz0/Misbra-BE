package com.Misbra.Entity;


import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import software.amazon.awssdk.annotations.NotNull;

import java.time.Instant;

@Data
@Document(collection = "categories")
public class Category {
    @Id
    private String categoryId;
    private String description;
    @NotNull
    private String categoryName;
    private String thumbnailPhotoId;
    private int numberOfGamesLeft;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
