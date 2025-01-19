package com.example.S3App;

import java.io.File;

/**
 * Defines operations for interacting with AWS S3 resources.
 */
public interface S3ResourceServiceInterface {

    /**
     * Lists the contents of a parent folder in S3, with possible pagination.
     * @param parent The parent resource (folder), or null for the bucket root.
     * @param cursor A continuation token from a previous call, or null for the first page.
     * @return A ListResult containing discovered resources plus possibly a next cursor.
     */
    ListResult<Resource> listFolder(Resource parent, String cursor);

    /**
     * Retrieves an S3 object's metadata by key. Throws an exception if it doesn't exist.
     * @param id The S3 key
     * @return A Resource (type=0 => file, type=1 => folder)
     */
    Resource getResource(String id);

    /**
     * Downloads the specified file resource to a local directory, preserving file name.
     * @param resource Must be type=0 (file).
     * @return A File reference to the downloaded content
     */
    File getAsFile(Resource resource);
}
