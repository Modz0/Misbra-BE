package com.Misbra.Service;

import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Enum.referenceType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PhotoService {

    PhotoDTO uploadPhoto(referenceType type, String referenceId, MultipartFile file, String userId) throws IOException;

    PhotoDTO uploadReviewPhoto(referenceType type, String referenceId, MultipartFile file, String userId, String reviewId) throws IOException;

    String getPresignedUrl(String photoId);

    Map<String, String> getBulkPresignedUrls(List<String> photoIds);

    PhotoDTO findPhotoById(String photoId);

    PhotoDTO updatePhoto(PhotoDTO photo);

    List<PhotoDTO> findPhotosById(List<String> photoIds);

    List<String> findPhotosByReviewId(String reviewId);
}
