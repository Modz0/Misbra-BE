package com.Misbra.Service;

import com.Misbra.DTO.CategoryDTO;
import com.Misbra.Entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    CategoryDTO getCategoryById(String id);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(String id, CategoryDTO categoryDTO);
}