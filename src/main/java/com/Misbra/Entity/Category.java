package com.Misbra.Entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
public class Category {
    @Id
    private String categoryId;
    private String description;
    private String categoryName;
    private String thumbnailPhotoId;
}
