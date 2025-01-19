package com.example.S3App;

import com.example.credentials.MyPropertiesCredentialsProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.identity.spi.ResolveIdentityRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MyPropertiesCredentialsProviderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLoadValidFile_shouldReturnCredentials() throws IOException {
        // 1) Create a temp properties file in test resources
        File tempProps = File.createTempFile("test-cred-", ".properties");
        try (FileWriter fw = new FileWriter(tempProps)) {
            fw.write("aws.accessKey=TEST_KEY\n");
            fw.write("aws.secretKey=TEST_SECRET\n");
        }

        // 2) copy it to the classpath or simulate classpath usage
        // For simplicity, let's just load it from an absolute path
        // and override getClass().getClassLoader().getResourceAsStream logic in a custom approach.
        // Alternatively, we can place a test resource in src/test/resources.

        MyPropertiesCredentialsProvider provider = new MyPropertiesCredentialsProvider(tempProps.getAbsolutePath()) {
            @Override
            protected String resourceAsPath(String resourceFile) {
                // We'll override a hypothetical method to handle absolute path usage
                return resourceFile;
            }
        };

        AwsCredentials creds = provider.resolveCredentials();
        assertNotNull(creds);
        assertEquals("TEST_KEY", creds.accessKeyId());
        assertEquals("TEST_SECRET", creds.secretAccessKey());

        tempProps.delete();
    }

    @Test
    public void testLoadMissingFile_shouldThrow() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Properties file not found");

        new MyPropertiesCredentialsProvider("application.properties") {
            @Override
            public Class<AwsCredentialsIdentity> identityType() {
                return super.identityType();
            }

            @Override
            public CompletableFuture<AwsCredentialsIdentity> resolveIdentity(ResolveIdentityRequest request) {
                return super.resolveIdentity(request);
            }

            @Override
            public CompletableFuture<? extends AwsCredentialsIdentity> resolveIdentity(Consumer<ResolveIdentityRequest.Builder> consumer) {
                return super.resolveIdentity(consumer);
            }

            @Override
            public CompletableFuture<? extends AwsCredentialsIdentity> resolveIdentity() {
                return super.resolveIdentity();
            }

            @Override
            protected String resourceAsPath(String resourceFile) {
                return "";
            }
        };
    }

    @Test
    public void testLoadFileMissingKey_shouldThrow() throws IOException {
        File tempProps = File.createTempFile("test-missing-key-", ".properties");
        try (FileWriter fw = new FileWriter(tempProps)) {
            fw.write("aws.accessKey=TEST_KEY\n");
            // no aws.secretKey
        }
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Missing 'aws.accessKey' or 'aws.secretKey'");

        new MyPropertiesCredentialsProvider(tempProps.getAbsolutePath()) {
            @Override
            protected String resourceAsPath(String resourceFile) {
                return resourceFile;
            }
        };
        // Clean up
        tempProps.delete();
    }
}
