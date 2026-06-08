package org.example.filestolink;

import java.util.List;
import java.time.ZonedDateTime;

public class FileData {

    private List<byte[]> fileBytes;
    private List<String> originalNames;
    private ZonedDateTime expTime;
    private String zipName;

    public FileData(List<byte[]> fileBytes, List<String> originalNames, ZonedDateTime expTime, String zipName) {
        this.fileBytes = fileBytes;
        this.originalNames = originalNames;
        this.expTime = expTime;
        this.zipName = zipName;
    }

    public List<byte[]> getFileBytes() { return fileBytes; }
    public List<String> getOriginalNames() { return originalNames; }
    public ZonedDateTime getExpTime() { return expTime; }
    public String getZipName() { return zipName; }
}