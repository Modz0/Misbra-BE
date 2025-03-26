package com.Misbra.Service;

import com.Misbra.DTO.CategoryDTO;
import com.Misbra.Entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Page<CategoryDTO> getAllCategories(Pageable pageable);
    CategoryDTO getCategoryById(String id);
    CategoryDTO createCategory(CategoryDTO categoryDTO ) ;
    CategoryDTO updateCategory(String id, CategoryDTO categoryDTO);
    void deleteCategory(String id); // Add this method
    String uploadCategotyThumbnail(String categoryID, MultipartFile file) throws IOException;
    void setThumbnail(String categoryId, String photoId);

}