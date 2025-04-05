package com.Misbra.Service;

import com.Misbra.DTO.PhotoDTO;
import com.Misbra.Enum.referenceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class S3ServiceImp implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // Constants for resizing
    private static final int STANDARD_WIDTH = 800;
    private static final int STANDARD_HEIGHT = 600;

    public S3ServiceImp(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Uploads an image file to AWS S3 and creates a corresponding PhotoDTO.
     * Now it also resizes the image before uploading.
     */
    @Override
    public PhotoDTO UploadImage(referenceType referenceType, String referenceId, MultipartFile file)
            throws IOException {

        // Basic null checks
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty.");
        }
        if (referenceId == null || referenceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reference ID cannot be null or empty.");
        }


        String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
        String s3Key = String.format(
                "%s/%s/%s.%s",
                referenceType,
                referenceId,
                UUID.randomUUID(),
                fileExtension
        );

        // 1. Resize the image
        byte[] resizedBytes = resizeImage(file.getBytes(), fileExtension);

        // 2. Upload the resized bytes to S3
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(resizedBytes));

        // 3. Generate a presigned URL valid for 7 days
        String imageUrl = generatePresignedUrl(s3Key);

        // 4. Build a PhotoDTO
        PhotoDTO photo = new PhotoDTO();
        photo.setS3key(s3Key);
        photo.setUploadedDate(LocalDateTime.now());
        photo.setType(referenceType);
        photo.setReferenceId(referenceId);
        photo.setIsVerified(false);
        photo.setPresignedUrl(imageUrl);
        photo.setPresignedUrlExpiration(LocalDateTime.now().plusDays(7));

        return photo;
    }

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

    /**
     * Deletes an object from S3 using its S3 key.
     */
    @Override
    public boolean deleteImage(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 key cannot be null or empty.");
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            return true;
        } catch (Exception e) {
            // log exception as needed
            return false;
        }
    }

    // ------------------------ Private Helper Methods ------------------------

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Resizes an image to 800x600 (max) while maintaining aspect ratio.
     */
    private byte[] resizeImage(byte[] originalBytes, String fileExtension) throws IOException {
        // Read as a BufferedImage
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (originalImage == null) {
            throw new IOException("Could not read image data");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Default to scaling by width
        int newWidth = STANDARD_WIDTH;
        int newHeight = (int) (((double) STANDARD_WIDTH / originalWidth) * originalHeight);

        // If the new height is beyond the max, recalc based on height
        if (newHeight > STANDARD_HEIGHT) {
            newHeight = STANDARD_HEIGHT;
            newWidth = (int) (((double) STANDARD_HEIGHT / originalHeight) * originalWidth);
        }

        // Create resized image
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        // Convert back to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, fileExtension, baos);
        return baos.toByteArray();
    }
}
