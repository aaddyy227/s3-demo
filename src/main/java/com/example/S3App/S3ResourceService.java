package com.example.S3App;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete S3 service that:
 *   - Lists folders/files in a bucket (paginated)
 *   - Retrieves metadata for a single object
 *   - Downloads file to a user-specified directory, preserving original filename
 */
public class S3ResourceService implements S3ResourceServiceInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ResourceService.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final String downloadPath;

    /**
     * @param credsProvider The AWS credentials (default chain or custom)
     * @param region The AWS region
     * @param bucketName The target S3 bucket name
     * @param downloadPath Local directory to place downloaded files
     */
    public S3ResourceService(AwsCredentialsProvider credsProvider, Region region,
                             String bucketName, String downloadPath) {
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(credsProvider)
                .build();

        this.bucketName = bucketName;
        this.downloadPath = downloadPath;

        LOGGER.info("S3ResourceService created. Bucket='{}', region='{}', downloadPath='{}'",
                bucketName, region, downloadPath);
    }

    @Override
    public ListResult<Resource> listFolder(Resource parent, String cursor) {
        String prefix = "";
        if (parent != null && parent.getType() == 1) {
            prefix = ensureTrailingSlash(parent.getId());
        }

        LOGGER.debug("Listing S3 folder. bucket={}, prefix={}, cursor={}", bucketName, prefix, cursor);

        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .delimiter("/")
                    .continuationToken(cursor)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            List<Resource> resources = new ArrayList<>();

            // Subfolders
            for (CommonPrefix cp : response.commonPrefixes()) {
                String folderKey = cp.prefix(); // e.g., "someFolder/"
                Resource folder = new Resource();
                folder.setId(folderKey);
                folder.setName(extractName(folderKey));
                folder.setType(1); // 1 => folder
                resources.add(folder);
            }

            // Files
            for (S3Object obj : response.contents()) {
                String key = obj.key();
                // Skip the "placeholder" object if it's exactly the prefix
                if (key.endsWith("/") && key.equals(prefix)) {
                    continue;
                }
                Resource file = new Resource();
                file.setId(key);
                file.setName(extractName(key));
                file.setType(0); // 0 => file
                resources.add(file);
            }

            ListResult<Resource> result = new ListResult<>();
            result.setResources(resources);
            result.setCursor(response.nextContinuationToken()); // for pagination
            return result;

        } catch (SdkException e) {
            LOGGER.error("Error listing objects. bucket={}, prefix={}, message={}",
                    bucketName, prefix, e.getMessage());
            throw new RuntimeException("Failed to list S3 objects: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource getResource(String id) {
        if (id == null) {
            throw new IllegalArgumentException("getResource: S3 key cannot be null");
        }
        LOGGER.debug("Retrieving metadata for S3 key={}", id);

        try {
            HeadObjectRequest headReq = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(id)
                    .build();
            s3Client.headObject(headReq);

            Resource resource = new Resource();
            resource.setId(id);
            resource.setName(extractName(id));
            resource.setType(id.endsWith("/") ? 1 : 0);
            return resource;

        } catch (NoSuchKeyException ex) {
            LOGGER.warn("S3 object not found: key={}", id);
            throw new RuntimeException("Resource not found: " + id, ex);
        } catch (SdkException e) {
            LOGGER.error("Error retrieving resource metadata. key={}, msg={}", id, e.getMessage());
            throw new RuntimeException("Failed to retrieve S3 resource: " + id, e);
        }
    }

    @Override
    public File getAsFile(Resource resource) {
        if (resource == null || resource.getId() == null) {
            throw new IllegalArgumentException("getAsFile: Resource or ID is null");
        }
        if (resource.getType() == 1) {
            throw new IllegalArgumentException("Cannot download a folder as a file: " + resource.getId());
        }

        String key = resource.getId();
        LOGGER.debug("Downloading S3 object. bucket={}, key={}", bucketName, key);

        try {
            String fileName = extractName(key);
            Path outputPath = Paths.get(downloadPath, fileName);

            // Ensure the parent directory exists
            java.nio.file.Files.createDirectories(outputPath.getParent());

            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // Stream-based download to avoid loading entire file in memory
            try (ResponseInputStream<GetObjectResponse> s3Stream =
                         s3Client.getObject(getReq, ResponseTransformer.toInputStream());
                 FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

                byte[] buffer = new byte[16_384];
                int bytesRead;
                while ((bytesRead = s3Stream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            LOGGER.info("Downloaded S3 object: key={} => {}", key, outputPath);
            return outputPath.toFile();

        } catch (IOException e) {
            LOGGER.error("I/O error writing the S3 download. key={}", key, e);
            throw new RuntimeException("I/O error while saving downloaded file: " + e.getMessage(), e);
        } catch (SdkException e) {
            LOGGER.error("AWS SDK error downloading file. key={}, msg={}", key, e.getMessage());
            throw new RuntimeException("Failed to download S3 file: " + key, e);
        }
    }

    // Utility to ensure trailing slash for "folder" keys
    private String ensureTrailingSlash(String key) {
        return key.endsWith("/") ? key : key + "/";
    }

    // Utility to extract the final name from a key. e.g. "folder/sub/file.txt" => "file.txt"
    private String extractName(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        String noSlash = key.endsWith("/") ? key.substring(0, key.length() - 1) : key;
        int lastSlash = noSlash.lastIndexOf('/');
        if (lastSlash == -1) {
            return noSlash;
        }
        return noSlash.substring(lastSlash + 1);
    }
}
