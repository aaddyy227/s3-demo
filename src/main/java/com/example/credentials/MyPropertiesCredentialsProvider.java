package com.example.credentials;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads "aws.accessKey" and "aws.secretKey" from a local .properties file on the classpath.
 * For example, "application.properties" containing:
 *   aws.accessKey=MY_KEY
 *   aws.secretKey=MY_SECRET
 *
 * Then you run with `-Dexec.args="file"` to use this provider.
 */
public class MyPropertiesCredentialsProvider implements AwsCredentialsProvider {

    private final AwsCredentials credentials;

    public MyPropertiesCredentialsProvider(String propertiesFileName) {
        Properties props = new Properties();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (is == null) {
                throw new RuntimeException("Properties file not found: " + propertiesFileName);
            }
            props.load(is);

            String accessKey = props.getProperty("aws.accessKey");
            String secretKey = props.getProperty("aws.secretKey");
            if (accessKey == null || secretKey == null) {
                throw new RuntimeException("Missing 'aws.accessKey' or 'aws.secretKey' in " + propertiesFileName);
            }
            credentials = AwsBasicCredentials.create(accessKey, secretKey);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + propertiesFileName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public AwsCredentials resolveCredentials() {
        return credentials;
    }
}
