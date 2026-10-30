package com.smartcompany.service.user;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.smartcompany.dto.file.PreSignedUrlDto;
import com.smartcompany.exception.AppException;

/**
 * Files on the server's own disk, with the same flow as S3: the browser gets a
 * signed upload URL (valid for 15 minutes) pointing at LocalFileController,
 * uploads the file there, and shows it from a plain URL. No cloud account
 * needed, so this is the default (app.storage.type=local).
 *
 * The signature (an HMAC of the key and the expiry time) makes sure only URLs
 * handed out by this service can be uploaded to, like a presigned S3 URL.
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    // URL prefix of LocalFileController, under the /api context path
    public static final String URL_PREFIX = "/api/files/local/";

    private static final long UPLOAD_URL_VALIDITY_SECONDS = 15 * 60;
    private static final long MAX_FILE_BYTES = 20L * 1024 * 1024; // same as nginx's limit

    private static final Set<String> FOLDERS = Set.of(
            "profile-images", "product-images", "factory-images",
            "warehouse-images", "vehicle-images", "company-docs");

    // generated names only (a UUID and an extension), so a key can never
    // reach outside the storage folder
    private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9-]{1,64}(\\.[A-Za-z0-9]{1,8})?");

    private final Path root;
    private final byte[] signingKey;

    public LocalFileStorageService(@Value("${app.storage.local.dir:uploads}") String dir,
                                   @Value("${security.jwt.token.secret-key:secret-key}") String secret) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        this.signingKey = secret.getBytes(StandardCharsets.UTF_8);
        Files.createDirectories(root);
    }

    @Override
    public PreSignedUrlDto generatePreSignedPutUrl(String originalFilename, String folder) {
        if (!FOLDERS.contains(folder)) {
            throw new AppException("Unknown folder", HttpStatus.BAD_REQUEST);
        }
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).replaceAll("[^A-Za-z0-9.]", "")
                : "";
        if (extension.length() > 9) extension = "";
        String key = folder + "/" + UUID.randomUUID() + extension;

        long expires = Instant.now().getEpochSecond() + UPLOAD_URL_VALIDITY_SECONDS;
        String url = URL_PREFIX + key + "?expires=" + expires + "&signature=" + sign(key, expires);
        return new PreSignedUrlDto(url, key);
    }

    @Override
    public String generatePreSignedGetUrl(String key) {
        return URL_PREFIX + key;
    }

    /** Saves an upload, if its URL was signed by us and hasn't expired. */
    public void store(String key, long expires, String signature, InputStream body) throws IOException {
        if (Instant.now().getEpochSecond() > expires
                || !MessageDigest.isEqual(sign(key, expires).getBytes(StandardCharsets.UTF_8),
                                          signature.getBytes(StandardCharsets.UTF_8))) {
            throw new AppException("Upload URL is invalid or has expired", HttpStatus.FORBIDDEN);
        }
        Path target = resolve(key);
        Files.createDirectories(target.getParent());

        // written to a temporary file first, so a failed or too large upload
        // never leaves half a file behind
        Path temporary = Files.createTempFile(target.getParent(), "upload-", ".part");
        try (InputStream in = body; OutputStream out = Files.newOutputStream(temporary)) {
            byte[] buffer = new byte[8192];
            long total = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                if (total > MAX_FILE_BYTES) {
                    throw new AppException("File is too large", HttpStatus.PAYLOAD_TOO_LARGE);
                }
                out.write(buffer, 0, read);
            }
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(temporary);
            throw e;
        }
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Path of a stored file, or an error for keys that aren't ours. */
    public Path resolve(String key) {
        int slash = key.indexOf('/');
        if (slash < 0
                || !FOLDERS.contains(key.substring(0, slash))
                || !FILE_NAME.matcher(key.substring(slash + 1)).matches()) {
            throw new AppException("Unknown file", HttpStatus.NOT_FOUND);
        }
        return root.resolve(key).normalize();
    }

    private String sign(String key, long expires) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
            byte[] digest = mac.doFinal((key + "\n" + expires).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }
}
