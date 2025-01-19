package com.example.S3App;

/**
 * Represents either a file or folder in S3.
 * type=0 => file, type=1 => folder
 */
public class Resource {
    private String id;   // The S3 key
    private String name; // user-friendly name extracted from key
    private int type;    // 0=file, 1=folder

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
}
