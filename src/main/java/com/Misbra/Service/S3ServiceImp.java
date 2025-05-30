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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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

        // Get original file extension for reference
        String originalFileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));

        // Check if the file format is supported
        validateImageFormat(originalFileExtension);

        // Now using WebP extension for the stored file for better compression
        String s3Key = String.format(
                "%s/%s/%s.%s",
                referenceType,
                referenceId,
                UUID.randomUUID(),
                "webp"  // Using WebP format instead of PNG
        );

        // Resize the image and convert to WebP using our updated resizeImage method
        byte[] resizedImageBytes = resizeImage(file.getBytes(), originalFileExtension);

        // Upload the resized WebP bytes to S3
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("image/webp")  // Set content type as WebP
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(resizedImageBytes));

        // Generate a presigned URL valid for 7 days
        String imageUrl = generatePresignedUrl(s3Key);

        // Build a PhotoDTO
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

    /**
     * Validates if the image format is supported
     */
    private void validateImageFormat(String fileExtension) {
        Set<String> supportedFormats = new HashSet<>(Arrays.asList(
                "jpg", "jpeg", "png", "gif", "bmp", "webp"
        ));

        if (!supportedFormats.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image format: " + fileExtension +
                    ". Supported formats are: " + String.join(", ", supportedFormats));
        }
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
     * Resizes an image and converts it to PNG format for consistent processing.
     * Maintains aspect ratio while resizing to fit within standard dimensions.
     */
    /**
     * Resizes an image and converts it to PNG format for consistent processing.
     * Supports various formats including WebP.
     */
    private byte[] resizeImage(byte[] originalBytes, String fileExtension) throws IOException {
        BufferedImage originalImage;

        // Read the image regardless of format
        try {
            originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (originalImage == null) {
                throw new IOException("Failed to read image data for format: " + fileExtension);
            }
        } catch (Exception e) {
            throw new IOException("Error processing image: " + e.getMessage(), e);
        }

        // Calculate new dimensions maintaining aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double widthRatio = (double) STANDARD_WIDTH / originalWidth;
        double heightRatio = (double) STANDARD_HEIGHT / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // Create a new buffered image with the target dimensions
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        // Draw the original image on the new one with scaling
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Always convert to WebP for better compression
        try {
            // Find a WebP writer
            ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();

            // Set compression if supported
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(0.80f); // Adjust quality (0.0-1.0) - 0.80 is a good balance
            }

            writer.setOutput(ImageIO.createImageOutputStream(outputStream));
            writer.write(null, new IIOImage(resizedImage, null, null), writeParam);
            writer.dispose();

            return outputStream.toByteArray();
        } catch (Exception e) {
            // Log the WebP conversion error
            System.err.println("WebP conversion failed, falling back to PNG: " + e.getMessage());

            // Fallback to PNG if WebP writing fails
            ImageIO.write(resizedImage, "png", outputStream);
            return outputStream.toByteArray();
        }
    }
}
