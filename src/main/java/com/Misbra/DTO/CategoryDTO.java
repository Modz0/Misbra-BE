package com.Misbra.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private String categoryName;
    private String description;
    private String categoryId;
    private String thumbnailPhotoId;
    private String ThumbnailUrl;
    private int numberOfGamesLeft;
}
