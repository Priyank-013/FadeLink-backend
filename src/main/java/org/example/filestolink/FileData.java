package org.example.filestolink;

import java.time.LocalDateTime;
import java.util.List;

public class FileData {

    private List<byte[]> fileBytes;
    private List<String> originalNames;
    private LocalDateTime expTime;
    private String zipName;

    public FileData(List<byte[]> fileBytes, List<String> originalNames, LocalDateTime expTime, String zipName) {
        this.fileBytes = fileBytes;
        this.originalNames = originalNames;
        this.expTime = expTime;
        this.zipName = zipName;
    }

    public List<byte[]> getFileBytes() { return fileBytes; }
    public List<String> getOriginalNames() { return originalNames; }
    public LocalDateTime getExpTime() { return expTime; }
    public String getZipName() { return zipName; }
}