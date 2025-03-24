package com.Misbra.Entity;


import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
public class Category {
    private String categoryId;
    private String name;
    private String thumbnailPhotoId;
}
