package com.Misbra.ServiceTest;

import com.Misbra.Service.S3ServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class S3ServiceImpTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    private S3ServiceImp s3ServiceImp;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        s3ServiceImp = new S3ServiceImp(s3Client, s3Presigner);
        // Set the private bucketName field via reflection.
        Field bucketNameField = S3ServiceImp.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(s3ServiceImp, "test-bucket");
    }

    @Test
    void testGeneratePresignedUrl() throws MalformedURLException {
        // Create a dummy PresignedGetObjectRequest that returns a valid URL.
        PresignedGetObjectRequest dummyRequest = mock(PresignedGetObjectRequest.class);
        URL dummyUrl = new URL("http://example.com/presigned");
        when(dummyRequest.url()).thenReturn(dummyUrl);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(dummyRequest);

        String url = s3ServiceImp.generatePresignedUrl("test/key.jpg");
        assertEquals("http://example.com/presigned", url);
    }
}
