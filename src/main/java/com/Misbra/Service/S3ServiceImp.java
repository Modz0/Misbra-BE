package com.Misbra.Service;

import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Enum.referenceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * S3ServiceImp provides functionalities to upload images to AWS S3
 * and to generate presigned URLs for accessing the uploaded files.
 */
@Service
public class S3ServiceImp implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Constructs a new S3ServiceImp instance with the required AWS S3 clients.
     *
     * @param s3Client      The S3 client used to interact with AWS S3.
     * @param s3Presigner   The S3 presigner used to generate presigned URLs.
     */
    public S3ServiceImp(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Uploads an image file to AWS S3 and creates a corresponding PhotoDTO.
     *
     * This method generates a unique S3 key using the provided reference type,
     * reference ID, and file extension. It then uploads the file to the specified bucket,
     * generates a presigned URL valid for 7 days, and constructs a PhotoDTO with metadata.
     *
     *
     * @param referenceType The type of reference (e.g., PLACE, MENU_ITEM) for which the photo is being uploaded.
     * @param referenceId   The unique ID of the reference entity.
     * @param file          The MultipartFile to be uploaded.
     * @param userId        The ID of the user uploading the image.
     * @return A PhotoDTO containing S3 key, presigned URL, upload date, and other metadata.
     * @throws IOException If an error occurs during file upload.
     */
    @Override
    public PhotoDTO UploadImage(referenceType referenceType, String referenceId, MultipartFile file, String userId)
            throws IOException {
        // TODO: Make the null checks here

        // Generate S3 key for the file
        String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
        String s3Key = String.format("%s/%s/images/%s.%s", referenceType, referenceId, UUID.randomUUID(), fileExtension);

        // Upload file to S3
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        // Generate a presigned URL for the uploaded object
        String imageURl = generatePresignedUrl(s3Key);

        // Create and populate PhotoDTO with metadata
        PhotoDTO photo = new PhotoDTO();
        photo.setS3key(s3Key);
        photo.setUploadedByUserId(userId);
        photo.setUploadedDate(LocalDateTime.now());
        photo.setType(referenceType);
        photo.setReferenceId(referenceId);
        photo.setIsVerified(false); // or true if auto-verified
        photo.setPresignedUrl(imageURl);
        photo.setPresignedUrlExpiration(LocalDateTime.now().plusDays(7));

        return photo;
    }

    /**
     * Generates a presigned URL for accessing an object in S3.
     *
     * The generated URL is valid for 7 days.
     *
     *
     * @param s3Key The S3 key of the object.
     * @return A presigned URL as a String.
     */
    @Override
    public String generatePresignedUrl(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    // ------------------------ Private Helper Methods ------------------------

    /**
     * Extracts the file extension from a given filename.
     *
     * @param filename The name of the file.
     * @return The file extension without the leading dot.
     */
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
