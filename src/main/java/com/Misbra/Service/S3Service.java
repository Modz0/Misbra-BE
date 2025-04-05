package com.Misbra.Service;

import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Enum.referenceType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {

    PhotoDTO UploadImage(referenceType entityType, String entityId, MultipartFile file) throws IOException;

    String generatePresignedUrl(String s3Key);

    /**
     * Deletes an image from the S3 bucket based on its S3 key.
     */
    boolean deleteImage(String s3Key);
}
