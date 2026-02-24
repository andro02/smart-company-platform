package com.smartcompany.controller;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcompany.dto.PreSignedUrlDto;
import com.smartcompany.service.ProfileImageService;

@RestController
@RequestMapping("/api/files")
public class ProfileImageController {

    @Autowired
    private ProfileImageService s3Service;

    // @PostMapping("/upload")
    // public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("key") String key, @RequestParam("username") String username) {
    //     try {
    //         String filename = s3Service.uploadFile(file, key, username);
    //         return ResponseEntity.ok(filename);
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
    //     }
    // }

    @PutMapping("/presigned-url/put")
    public ResponseEntity<PreSignedUrlDto> getUploadPreSignedUrl(@RequestParam("key") String key, @RequestParam("username") String username) {
        PreSignedUrlDto preSignedUrlDto = s3Service.generatePreSignedPutUrl(key, username);
        return ResponseEntity.ok(preSignedUrlDto);
    }

    @GetMapping("/presigned-url/get")
    public ResponseEntity<String> getDownloadGetPreSignedUrl(@RequestParam("key") String key) {
        URL url = s3Service.generatePreSignedGetUrl(key);
        return ResponseEntity.ok(url.toString());
    }
}
