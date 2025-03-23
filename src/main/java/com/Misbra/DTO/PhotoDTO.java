package com.Misbra.DTO;

import com.Misbra.Enum.referenceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO
{

    private String photoId;
    private String s3key;
    private String uploadedByUserId;
    private String reviewId;
    private LocalDateTime uploadedDate;
    private String referenceId;
    private referenceType type;// place / menu item
    private Boolean isVerified;
    private String presignedUrl;
    private LocalDateTime presignedUrlExpiration;
}
