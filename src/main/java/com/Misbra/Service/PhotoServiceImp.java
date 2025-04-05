package com.Misbra.Service;

import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Entity.Photo;
import com.Misbra.Enum.referenceType;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.PhotoMapper;
import com.Misbra.Repository.PhotoRepository;
import com.Misbra.Utils.BusinessMessageKeys;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhotoServiceImp implements PhotoService {

    private final PhotoRepository photoRepository;
    private final PhotoMapper photoMapper;
    private final ExceptionUtils exceptionUtils;
    private final S3Service s3Service;

    /**
     * Constructs a new PhotoServiceImp with the required dependencies.
     *
     * @param photoRepository Repository for photo entities.
     * @param photoMapper Mapper to convert between PhotoDTO and Photo.
     * @param exceptionUtils Utility for exception handling.
     * @param s3Service Service for interacting with AWS S3.
     */
    public PhotoServiceImp(PhotoRepository photoRepository,
                           PhotoMapper photoMapper,
                           ExceptionUtils exceptionUtils,
                           S3Service s3Service) {
        this.photoRepository = photoRepository;
        this.photoMapper = photoMapper;
        this.exceptionUtils = exceptionUtils;
        this.s3Service = s3Service;
    }

    /**
     * Uploads a photo to AWS S3 for a given reference and returns a PhotoDTO.
     *
     * @param type The type of reference (e.g. PLACE, MENU_ITEM).
     * @param referenceId The ID of the reference entity.
     * @param file The file to upload.
     * @return The PhotoDTO representing the uploaded photo.
     * @throws IOException If an error occurs during file upload.
     */
    @Override
    public PhotoDTO uploadPhoto(referenceType type, String referenceId, MultipartFile file) throws IOException {
        PhotoDTO newPhoto = s3Service.UploadImage(type, referenceId, file);
        Photo entity = photoMapper.toEntity(newPhoto);
        entity = photoRepository.save(entity);
        return photoMapper.toDTO(entity);
    }

    /**
     * Uploads a review photo to AWS S3 for a given reference and review, and returns a PhotoDTO.
     *
     * @param type The type of reference (e.g. PLACE, MENU_ITEM).
     * @param referenceId The ID of the reference entity.
     * @param file The file to upload.
     * @param reviewId The ID of the review associated with the photo.
     * @return The PhotoDTO representing the uploaded review photo.
     * @throws IOException If an error occurs during file upload.
     */
    @Override
    public PhotoDTO uploadReviewPhoto(referenceType type, String referenceId, MultipartFile file, String reviewId) throws IOException {
        PhotoDTO newPhoto = s3Service.UploadImage(type, referenceId, file);
        newPhoto.setReviewId(reviewId);
        Photo entity = photoMapper.toEntity(newPhoto);
        photoRepository.save(entity);
        return newPhoto;
    }

    /**
     * Retrieves the presigned URL for a photo given its photoId.
     * If the URL has expired, a new one is generated.
     *
     * @param photoId The ID of the photo.
     * @return The valid presigned URL as a String.
     */
    @Override
    public String getPresignedUrl(String photoId) {
        PhotoDTO photo = findPhotoById(photoId);
        if (isUrlExpired(photo)) {
            regenerateUrl(photo);
        }
        return photo.getPresignedUrl();
    }

    /**
     * Retrieves a mapping of photo IDs to their presigned URLs in bulk.
     * For each photo, if the URL is expired, it is regenerated.
     *
     * @param photoIds A list of photo IDs.
     * @return A map where keys are photo IDs and values are presigned URLs.
     */
    @Override
    public Map<String, String> getBulkPresignedUrls(List<String> photoIds) {
        // 1. Fetch entities from repository
        List<Photo> photoEntities = photoRepository.findAllByPhotoIdIn(photoIds);

        // 2. Convert to DTOs and process
        return photoEntities.stream()
                .map(photoMapper::toDTO) // Convert Entity to DTO
                .collect(Collectors.toMap(
                        PhotoDTO::getPhotoId,
                        photo -> {
                            if (isUrlExpired(photo)) {
                                return regenerateUrl(photo);
                            }
                            return photo.getPresignedUrl();
                        },
                        // Merge function for duplicate keys (keep existing)
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Finds a photo by its ID.
     * Throws a validation exception if the photo is not found or if the photoId is null.
     *
     * @param photoId The ID of the photo.
     * @return The PhotoDTO representing the found photo.
     */
    @Override
    public PhotoDTO findPhotoById(String photoId) {
        // Validate input
        if (photoId == null) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PHOTO_NOT_FOUND,
                    new String[]{"photoId is null"}
            ));
            exceptionUtils.throwValidationException(errors);
            return null;
        }

        Optional<Photo> entityOpt = photoRepository.findById(photoId);
        if (entityOpt.isEmpty()) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PHOTO_NOT_FOUND,
                    new String[]{"No photo with ID: " + photoId}
            ));
            exceptionUtils.throwValidationException(errors);
            return null; // Unreachable
        }

        return photoMapper.toDTO(entityOpt.get());
    }

    /**
     * Updates an existing photo's details.
     * Validates input and existence of the photo before updating.
     *
     * @param photoDTO The PhotoDTO containing updated photo details.
     * @return The updated PhotoDTO.
     */
    @Override
    public PhotoDTO updatePhoto(PhotoDTO photoDTO) {
        // Validate input
        if (photoDTO == null) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PHOTO_UPDATE_FAILED,
                    new String[]{"PhotoDTO is null"}
            ));
            exceptionUtils.throwValidationException(errors);
            return null; // Unreachable
        }
        if (photoDTO.getPhotoId() == null) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PHOTO_UPDATE_FAILED,
                    new String[]{"photoId is null"}
            ));
            exceptionUtils.throwValidationException(errors);
            return null; // Unreachable
        }

        // Check if the photo exists
        Optional<Photo> existingOpt = photoRepository.findById(photoDTO.getPhotoId());
        if (existingOpt.isEmpty()) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PHOTO_UPDATE_FAILED,
                    new String[]{"No photo found with ID: " + photoDTO.getPhotoId()}
            ));
            exceptionUtils.throwValidationException(errors);
            return null; // Unreachable
        }

        // Save and return updated entity
        Photo updatedEntity = photoRepository.save(photoMapper.toEntity(photoDTO));
        return photoMapper.toDTO(updatedEntity);
    }

    /**
     * Finds photos by a list of photo IDs.
     *
     * @param photoIds A list of photo IDs.
     * @return A list of PhotoDTO objects corresponding to the provided IDs.
     */
    @Override
    public List<PhotoDTO> findPhotosById(List<String> photoIds) {
        // If no IDs provided, return empty list (not an error)
        if (photoIds == null || photoIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Load photos from repository
        List<Photo> entities = photoRepository.findAllById(photoIds);

        // Return DTOs for found photos
        return entities.stream()
                .map(photoMapper::toDTO)
                .toList();
    }

    /**
     * Finds photos associated with a given review ID and returns their presigned URLs.
     *
     * @param reviewId The ID of the review.
     * @return A list of presigned URLs for photos associated with the review.
     */
    @Override
    public List<String> findPhotosByReviewId(String reviewId) {
        List<String> photoUrls = new ArrayList<>();
        photoRepository.findByReviewId(reviewId)
                .forEach(x -> photoUrls.add(x.getPresignedUrl()));
        return photoUrls;
    }

    @Override
    public boolean deletePhotoById(String photoId) {
        if (photoId == null) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PHOTO_NOT_FOUND,
                    new String[]{"photoId is null"}
            ));
            exceptionUtils.throwValidationException(errors);
            return false; // unreachable
        }

        Optional<Photo> photoOpt = photoRepository.findById(photoId);
        if (photoOpt.isEmpty()) {
            // Throw your custom not-found exception or return false
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.PHOTO_NOT_FOUND,
                    new String[]{"No photo with ID: " + photoId}
            ));
            exceptionUtils.throwValidationException(errors);
            return false; // unreachable
        }

        Photo photoEntity = photoOpt.get();
        String s3Key = photoEntity.getS3key();
        boolean deletedFromS3 = s3Service.deleteImage(s3Key);
        if (deletedFromS3) {
            // Only delete from DB if S3 deletion was successful (optional logic)
            photoRepository.delete(photoEntity);
        }
        return deletedFromS3;
    }

    // --------------------- Private Helper Methods ---------------------

    /**
     * Regenerates a presigned URL for a given photo if it has expired.
     * Updates the PhotoDTO with the new URL and its expiration time, and saves the update.
     *
     * @param photo The PhotoDTO for which to regenerate the URL.
     * @return The newly generated presigned URL.
     */
    private String regenerateUrl(PhotoDTO photo) {
        // Generate new URL
        String newUrl = s3Service.generatePresignedUrl(photo.getS3key());

        // Update DTO
        photo.setPresignedUrl(newUrl);
        photo.setPresignedUrlExpiration(LocalDateTime.now().plus(Duration.ofDays(7)));

        // Save updated photo to database
        photoRepository.save(photoMapper.toEntity(photo));

        return newUrl;
    }

    /**
     * Checks if the presigned URL for a given photo has expired.
     *
     * @param photo The PhotoDTO to check.
     * @return true if the URL is expired or not set, false otherwise.
     */
    private boolean isUrlExpired(PhotoDTO photo) {
        return photo.getPresignedUrlExpiration() == null
                || photo.getPresignedUrlExpiration().isBefore(LocalDateTime.now());
    }
}
