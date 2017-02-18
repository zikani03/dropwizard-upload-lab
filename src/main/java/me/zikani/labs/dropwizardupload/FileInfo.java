package me.zikani.labs.dropwizardupload;

import java.time.LocalDate;

public class FileInfo {
    String fileName;

    long size;

    LocalDate createdAt;

    public FileInfo() {
    }

    public FileInfo(String fileName, long size, LocalDate createdAt) {
        this.fileName = fileName;
        this.size = size;
        this.createdAt = createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
