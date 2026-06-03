package org.example.filestolink;

import java.util.List;

public class UploadResponse {

    private String message;
    private String viewLink;
    private String downloadLink;
    private String expiry;
    private List<String> files;

    public UploadResponse(String message,
                          String viewLink,
                          String downloadLink,
                          String expiry,
                          List<String> files) {

        this.message = message;
        this.viewLink = viewLink;
        this.downloadLink = downloadLink;
        this.expiry = expiry;
        this.files = files;
    }

    public String getMessage() {
        return message;
    }

    public String getViewLink() {
        return viewLink;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getExpiry() {
        return expiry;
    }

    public List<String> getFiles() {
        return files;
    }
}