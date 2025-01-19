package com.example.S3App;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Demonstrates:
 *   - Loading config (region, bucket, objectKey, downloadPath) from application.properties
 *   - Switching credential mode: default vs. file-based
 *   - Listing an S3 bucket with pagination
 *   - Retrieving and downloading a file to a specified folder
 */
public class S3DemoApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3DemoApp.class);

    public static void main(String[] args) {
        // 1) Decide if we use "env" (default chain) or "file" (MyPropertiesCredentialsProvider)
        String credentialMode = (args.length > 0) ? args[0] : "env";

        // 2) Load "application.properties" for region/bucket/objectKey/downloadPath
        Properties appProps = loadProperties();
        String regionStr = appProps.getProperty("aws.s3.region");
        String bucketName = appProps.getProperty("aws.s3.bucketName");
        String objectKey = appProps.getProperty("aws.s3.objectKey");
        String downloadPath = appProps.getProperty("aws.s3.downloadPath", ".");

        Region region = Region.of(regionStr);
        LOGGER.info("Using region='{}', bucket='{}', objectKey='{}', downloadPath='{}'",
                regionStr, bucketName, objectKey, downloadPath);

        // 3) Build credentials provider
        AwsCredentialsProvider credsProvider;
        if ("file".equalsIgnoreCase(credentialMode)) {
            // Load from myapp.properties (or whichever .properties you want)
            LOGGER.info("Using MyPropertiesCredentialsProvider from 'application.properties'.");
            credsProvider = new com.example.credentials.MyPropertiesCredentialsProvider("application.properties");
        } else {
            // default chain: environment variables, ~\.aws\credentials, IAM role, etc.
            LOGGER.info("Using default credentials chain (env vars, profiles, IAM).");
            credsProvider = DefaultCredentialsProvider.create();
        }

        // 4) Validate credentials
        if (!checkCredentials(credsProvider)) {
            LOGGER.error("No valid AWS credentials found. Exiting...");
            System.exit(1);
        }

        // 5) Build S3 resource service
        S3ResourceServiceInterface s3Service = new S3ResourceService(
                credsProvider, region, bucketName, downloadPath
        );
        LOGGER.info("=== AWS S3 Demo with pagination (region={}, bucket={}) ===", regionStr, bucketName);

        // 6) Wait for user input to list
        System.out.println("Press [ENTER] to list the bucket (paginated)...");
        new Scanner(System.in).nextLine();

        // 7) Paginated listing
        listAllPages(s3Service);

        // 8) Retrieve & download the specified object from application.properties
        System.out.println("\nAttempting to get resource metadata for key: " + objectKey);
        try {
            Resource res = s3Service.getResource(objectKey);
            System.out.println("Resource found: " + res.getName() + " (type=" + res.getType() + ")");
            if (res.getType() == 0) { // 0 => file
                File downloadedFile = s3Service.getAsFile(res);
                System.out.println("Downloaded file to: " + downloadedFile.getAbsolutePath());
            } else {
                System.out.println("That key is a folder, skipping download...");
            }
        } catch (RuntimeException e) {
            LOGGER.error("Error retrieving/downloading resource: {}", e.getMessage());
        }

        LOGGER.info("Demo completed.");
    }

    /**
     * Lists the entire bucket in pages until there's no cursor.
     */
    private static void listAllPages(S3ResourceServiceInterface s3Service) {
        String cursor = null;
        int pageNumber = 1;

        do {
            LOGGER.info("Listing page #{}", pageNumber);
            ListResult<Resource> result = s3Service.listFolder(null, cursor);
            List<Resource> items = result.getResources();

            System.out.printf("\n--- Page #%d ---\n", pageNumber);
            for (Resource r : items) {
                String typeLabel = (r.getType() == 0) ? "File" : "Folder";
                System.out.printf(" - Name: %s, Key: %s, Type: %s%n",
                        r.getName(), r.getId(), typeLabel);
            }

            cursor = result.getCursor();
            pageNumber++;
        } while (cursor != null);
    }

    /**
     * Loads a .properties file (application.properties) from the classpath.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = S3DemoApp.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new IOException("Properties file not found: " + "application.properties");
            }
            props.load(is);
        } catch (IOException e) {
            LOGGER.error("Failed to load '{}': {}", "application.properties", e.getMessage());
            System.exit(1);
        }
        return props;
    }

    /**
     * Checks if credentials can be resolved. If not, we exit early.
     */
    private static boolean checkCredentials(AwsCredentialsProvider credsProvider) {
        try {
            credsProvider.resolveCredentials();
            return true;
        } catch (SdkClientException e) {
            LOGGER.error("AWS credentials check failed: {}", e.getMessage());
            return false;
        }
    }
}
