package com.example.S3App;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for S3ResourceService using a mock S3Client.
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class S3ResourceServiceTest {

    @Mock
    private S3Client mockS3Client;

    @Mock
    private AwsCredentialsProvider mockCredsProvider;

    // The service under test
    private S3ResourceService service;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String DOWNLOAD_PATH = "C:/temp/downloads";

    @Before
    public void setUp() {
        // We pass in the mockS3Client via constructor injection if we had that,
        // but in the current code, S3ResourceService always builds its own S3Client.
        // For testing, let's do a partial approach: we can create S3ResourceService
        // and then use reflection or alter the code to accept a "protected constructor".
        // For illustration, we'll do a direct "override" approach:

        service = new S3ResourceService(mockCredsProvider, Region.US_EAST_1, BUCKET_NAME, DOWNLOAD_PATH) {
            @Override
            protected S3Client buildS3Client(AwsCredentialsProvider creds, Region region) {
                // We'll override the method that constructs the real S3Client
                // and return our mock instead.
                return mockS3Client;
            }
        };
    }





    @Test(expected = RuntimeException.class)
    public void testGetResource_givenNoSuchKey_shouldThrowException() {
        // Arrange
        // If the object doesn't exist, the SDK throws a NoSuchKeyException
        when(mockS3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("Not found").build());

        // Act
        service.getResource("missing-file.txt");
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void testGetAsFile_givenFolder_shouldThrow() {
        // Arrange
        Resource folderRes = new Resource();
        folderRes.setId("someFolder/");
        folderRes.setName("someFolder");
        folderRes.setType(1); // folder

        // Act
        service.getAsFile(folderRes);
    }
}
