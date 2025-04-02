package com.Misbra.Entity;


import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import software.amazon.awssdk.annotations.NotNull;

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

}
