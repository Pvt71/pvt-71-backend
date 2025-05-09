package com.pvt.project71.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ImageValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; //10MB
    private static final List<String> ALLOWED_FILE_TYPES = List.of("jpg", "jpeg", "png");

    public void validate(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename.isBlank()) {
            throw new IllegalArgumentException("File must have a name.");
        }

        String extension = getExtension(filename).toLowerCase();
        if (!ALLOWED_FILE_TYPES.contains(extension)) {
            throw new IllegalArgumentException("Invalid file type. Only JPG and PNG are allowed.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File is too large. Max size is 10 MB.");
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
}