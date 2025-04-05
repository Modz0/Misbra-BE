package com.Misbra.Service;

import com.Misbra.DTO.CategoryDTO;
import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Entity.Category;
import com.Misbra.Enum.referenceType;
import com.Misbra.Mapper.CategoryMapper;
import com.Misbra.Repository.CategoryRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImp implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final PhotoService photoService;
    private final QuestionService questionService;

    @Autowired
    public CategoryServiceImp(CategoryRepository categoryRepository, CategoryMapper categoryMapper, PhotoService photoService, QuestionService questionService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.photoService = photoService;
        this.questionService = questionService;
    }

    @Override
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        // Get paginated data directly from repository
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        // Convert entities to DTOs while preserving pagination
        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());

        // Gather all thumbnail IDs from this page only
        List<String> thumbnailIds = categoryDTOs.stream()
                .map(CategoryDTO::getThumbnailPhotoId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Get presigned URLs for thumbnails in this page only
        if (!thumbnailIds.isEmpty()) {
            Map<String, String> presignedUrls = photoService.getBulkPresignedUrls(thumbnailIds);
            for (CategoryDTO category : categoryDTOs) {
                if (category.getThumbnailPhotoId() != null &&
                        presignedUrls.containsKey(category.getThumbnailPhotoId())) {
                    category.setThumbnailUrl(presignedUrls.get(category.getThumbnailPhotoId()));
                }
            }
        }

        // Return a new page with the same pagination metadata but with DTOs as content
        return new PageImpl<>(categoryDTOs, pageable, categoryPage.getTotalElements());
    }

    @Override
    public Page<CategoryDTO> getAllCategoriesForUser(Pageable pageable, String userId) {
        // Get paginated data directly from repository
        Page<Category> categoryPage = categoryRepository.findAll(pageable);


        // Convert entities to DTOs while preserving pagination
        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());

        categoryDTOs.forEach(categoryDTO -> {categoryDTO.setNumberOfGamesLeft(questionService.calculateAvailableGamesCount(categoryDTO.getCategoryId(), userId));});


        // Gather all thumbnail IDs from this page only
        List<String> thumbnailIds = categoryDTOs.stream()
                .map(CategoryDTO::getThumbnailPhotoId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Get presigned URLs for thumbnails in this page only
        if (!thumbnailIds.isEmpty()) {
            Map<String, String> presignedUrls = photoService.getBulkPresignedUrls(thumbnailIds);
            for (CategoryDTO category : categoryDTOs) {
                if (category.getThumbnailPhotoId() != null &&
                        presignedUrls.containsKey(category.getThumbnailPhotoId())) {
                    category.setThumbnailUrl(presignedUrls.get(category.getThumbnailPhotoId()));
                }
            }
        }

        // Return a new page with the same pagination metadata but with DTOs as content
        return new PageImpl<>(categoryDTOs, pageable, categoryPage.getTotalElements());
    }


    @Override
    public CategoryDTO getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
         CategoryDTO categoryDTO = categoryMapper.toDTO(category);  // Use mapper
        if(categoryDTO.getThumbnailPhotoId()!=null){
            String thumbnailUrl = photoService.getPresignedUrl(categoryDTO.getThumbnailPhotoId());
            categoryDTO.setThumbnailUrl(thumbnailUrl);
        }
        return categoryDTO;
    }

    @Override
     public  List<CategoryDTO> getListOfCategories (List<String> categoryIds) {

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        List<CategoryDTO> categoryDTOS = categories.stream().map(categoryMapper::toDTO).collect(Collectors.toList());
        for (CategoryDTO categoryDTO : categoryDTOS) {
            if (categoryDTO.getThumbnailPhotoId() != null) {
                String thumbnailUrl = photoService.getPresignedUrl(categoryDTO.getThumbnailPhotoId());
                categoryDTO.setThumbnailUrl(thumbnailUrl);
            }

        }
        return categoryDTOS;
     }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO)  {

        Category category = categoryMapper.toEntity(categoryDTO);  // Use mapper
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDTO(savedCategory);  // Use mapper
    }

    @Override
    public CategoryDTO updateCategory(String id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Update the fields
        existingCategory.setCategoryName(categoryDTO.getCategoryName());

        if(!ObjectUtils.isEmpty(existingCategory.getThumbnailPhotoId())){
            photoService.deletePhotoById(existingCategory.getThumbnailPhotoId());
        }


        existingCategory.setThumbnailPhotoId(categoryDTO.getThumbnailPhotoId());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDTO(updatedCategory);  // Use mapper
    }

    @Transactional
    @Override
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        questionService.findQuestionByCategory(id).forEach(question -> {
            questionService.deleteQuestion(question.getQuestionId());
        });
        if(category.getThumbnailPhotoId()!=null){
            photoService.deletePhotoById(category.getThumbnailPhotoId());
        }
        categoryRepository.delete(category);
    }


    @Override
    public String uploadCategotyThumbnail(String categoryID, MultipartFile file) throws IOException {
        PhotoDTO newPhoto = photoService.uploadPhoto(referenceType.CATEGORY, categoryID, file);
        setThumbnail(categoryID, newPhoto.getPhotoId());
        return photoService.getPresignedUrl(newPhoto.getPhotoId());
    }
    @Override
    public void setThumbnail(String categoryId, String photoId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        category.setThumbnailPhotoId(photoId);
        categoryRepository.save(category);

    }


}