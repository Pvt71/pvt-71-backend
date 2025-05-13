package com.pvt.project71.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class ImageValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; //10MB
    private static final List<String> ALLOWED_FILE_TYPES = List.of("jpg", "jpeg", "png");

    public void validate(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have a name.");
        }

        String extension = getExtension(filename).toLowerCase();
        if (!ALLOWED_FILE_TYPES.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file type. Allowed types are: " + String.join(", ", ALLOWED_FILE_TYPES));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is too large. Max size is 10 MB.");
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
}