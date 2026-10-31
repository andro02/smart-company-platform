package com.smartcompany.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcompany.service.user.LocalFileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Upload and download of files kept on the server's disk (app.storage.type=
 * local). The URLs come from LocalFileStorageService, the same way presigned
 * URLs come from S3, so the frontend works with either storage unchanged.
 */
@RestController
@RequestMapping("/files/local")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@Tag(name = "File Storage", description = "Fajlovi na disku servera (umesto S3)")
public class LocalFileController {

    private final LocalFileStorageService storage;

    public LocalFileController(LocalFileStorageService storage) {
        this.storage = storage;
    }

    @Operation(summary = "Upload fajla na potpisani URL (kao presigned S3 PUT)")
    @SecurityRequirements
    @PutMapping("/{folder}/{name:.+}")
    public ResponseEntity<Void> upload(@PathVariable String folder,
                                       @PathVariable String name,
                                       @RequestParam long expires,
                                       @RequestParam String signature,
                                       HttpServletRequest request) throws IOException {
        storage.store(folder + "/" + name, expires, signature, request.getInputStream());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Prikaz/preuzimanje fajla")
    @SecurityRequirements
    @GetMapping("/{folder}/{name:.+}")
    public ResponseEntity<Resource> download(@PathVariable String folder, @PathVariable String name) {
        return serve(storage.resolve(folder + "/" + name), name);
    }

    // files from older data that were saved without a folder
    @Operation(summary = "Prikaz/preuzimanje fajla bez foldera (stariji podaci)")
    @SecurityRequirements
    @GetMapping("/{name:.+}")
    public ResponseEntity<Resource> downloadTopLevel(@PathVariable String name) {
        return serve(storage.resolve(name), name);
    }

    private ResponseEntity<Resource> serve(Path file, String name) {
        if (!Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        // keys are random and never reused, so a file can be cached for long
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(name).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(new FileSystemResource(file));
    }
}
