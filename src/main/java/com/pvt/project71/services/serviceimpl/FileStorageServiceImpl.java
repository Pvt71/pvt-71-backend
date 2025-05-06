package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final String IMAGE_DIR = "src/main/resources/public/images/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; //10MB
    private static final List<String> ALLOWED_FILE_TYPES = List.of("jpg", "jpeg", "png");

    public String saveImage(MultipartFile file) throws IOException {
        validateFile(file);

        Files.createDirectories(Paths.get(IMAGE_DIR));
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Path filePath = Paths.get(IMAGE_DIR + filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/images/" + filename;
    }

    public boolean deleteImage(String filename) throws IOException {
        Path filePath = Paths.get(IMAGE_DIR + filename);
        return Files.deleteIfExists(filePath);
    }

    private void validateFile(MultipartFile file) {
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
        if (dotIndex == -1) return "";
        return filename.substring(dotIndex + 1);
    }
}
