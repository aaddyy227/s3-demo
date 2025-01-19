package com.example.S3App;

import java.util.List;

/**
 * Simple container for a list of resources plus a cursor for pagination.
 */
public class ListResult<T> {
    private List<T> resources;
    private String cursor; // Next continuation token, or null if last page

    public List<T> getResources() {
        return resources;
    }
    public void setResources(List<T> resources) {
        this.resources = resources;
    }

    public String getCursor() {
        return cursor;
    }
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
