package com.smartcompany.service.user;

import com.smartcompany.dto.file.PreSignedUrlDto;

/**
 * Where uploaded files (images, company documents) are kept. The browser asks
 * for a URL to upload a file to, uploads it there itself, and later asks for a
 * URL to show it from.
 *
 * Two implementations, picked with app.storage.type:
 *   local (default) - on the server's own disk (LocalFileStorageService)
 *   s3              - in an AWS S3 bucket (S3FileStorageService)
 */
public interface FileStorageService {

    /**
     * A URL the browser can PUT a new file to, and the generated key of the
     * file, which the frontend then sends in its create/update request.
     *
     * @param originalFilename original filename (used to extract extension)
     * @param folder           e.g. "profile-images", "product-images"
     */
    PreSignedUrlDto generatePreSignedPutUrl(String originalFilename, String folder);

    /** A URL to show or download the file with the given key. */
    String generatePreSignedGetUrl(String key);
}
