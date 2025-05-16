package com.pvt.project71.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class WellKnownForwardController {

    @GetMapping("/.well-known/assetlinks.json")
    public ResponseEntity<Resource> forwardAssetLinks() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/.well-known/assetlinks.json");

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "application/json")
                .body(resource);
    }
}