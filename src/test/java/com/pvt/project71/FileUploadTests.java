package com.pvt.project71;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileUploadTests {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void cleanupUploadedFiles() throws Exception {
        Path imageDir = Paths.get("src/main/resources/public/images/");
        if (Files.exists(imageDir)) {
            Files.walk(imageDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            System.err.println("Failed to delete test file: " + path);
                        }
                    });
        }
    }


    @Test
    public void testThatValidImageUploadReturnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "banner.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().string(Matchers.containsString("/images/banner.jpg")));

    }

    @Test
    public void testThatUploadOfTooLargeImageReturnsBadRequest() throws Exception {
        byte[] largeFile = new byte[11 * 1024 * 1024]; //11MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", largeFile);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/image")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect((ResultMatcher) content().string(Matchers.containsString("File is too large. Max size is 10 MB.")));
    }

    @Test
    public void testThatUploadOfInvalidFileTypeReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "testFile.exe", "application/octet-stream", new byte[1024]);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/uploads/image")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(Matchers.containsString("Invalid file type")));
    }
}