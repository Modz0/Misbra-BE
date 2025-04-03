package com.Misbra.Entity;

import com.Misbra.Enum.referenceType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import software.amazon.awssdk.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Document(collection = "photos")
public class Photo {

    @Id
    private String photoId;

    private String s3key;
    private String uploadedByUserId;
    private LocalDateTime uploadedDate;
    @NotNull
    private String referenceId;
    private referenceType type;// place / menu item
    private Boolean isVerified;
    private String reviewId;
    private String presignedUrl;
    private LocalDateTime presignedUrlExpiration;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}
