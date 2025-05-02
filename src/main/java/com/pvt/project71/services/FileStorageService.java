package com.pvt.project71.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
public interface FileStorageService {


    public String saveImage(MultipartFile file) throws IOException;

    public boolean deleteImage(String filename) throws IOException;
}