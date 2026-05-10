package eu.ellipse.backend.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.awt.Image;

@Service
public class AvatarStorageService {

    private static final Logger log = LoggerFactory.getLogger(AvatarStorageService.class);

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, String> MIME_TO_EXT = Map.of(
            "image/jpeg", ".jpg",
            "image/png",  ".png",
            "image/gif",  ".gif"
    );

    private static final Map<String, byte[]> EXT_TO_MAGIC = Map.of(
            ".jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            ".png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            ".gif", new byte[]{0x47, 0x49, 0x46, 0x38}
    );

    private final String avatarDirectory;
    private Path root;

    public AvatarStorageService(
            @Value("${ellipse.avatars.directory}") String avatarDirectory
    ) {
        this.avatarDirectory = avatarDirectory;
    }

    @PostConstruct
    public void init() throws IOException {
        root = Path.of(avatarDirectory).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }


    public String store(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("AVATAR_TOO_LARGE");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("INVALID_AVATAR");
        }

        String normalizedType = contentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
        String ext = MIME_TO_EXT.get(normalizedType);
        if (ext == null) {
            throw new RuntimeException("INVALID_AVATAR");
        }

        validateMagicBytes(file, ext);

        String filename = UUID.randomUUID() + ext;
        Path dest = root.resolve(filename);
        
        if (!dest.normalize().startsWith(root)) throw new IOException("Invalid path");
        
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        return filename;
    }

    public void deleteIfExists(String avatarId) {
        if (avatarId == null || avatarId.isBlank()) {
            return;
        }
        Path path = root.resolve(avatarId).normalize();
        if (!path.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete avatar '{}': {}", avatarId, e.getMessage());
        }
    }

    /**
     * Reads the first few bytes of the upload and checks them against the
     * expected magic bytes for the given extension.
     *
     * @throws RuntimeException with "INVALID_AVATAR" if the content doesn't match
     */
    private void validateMagicBytes(MultipartFile file, String ext) throws IOException {
        byte[] expected = EXT_TO_MAGIC.get(ext);
        if (expected == null) {
            throw new RuntimeException("INVALID_AVATAR");
        }

        byte[] header = new byte[expected.length];
        try (InputStream in = file.getInputStream()) {
            int bytesRead = in.read(header, 0, expected.length);
            if (bytesRead < expected.length) {
                throw new RuntimeException("INVALID_AVATAR");
            }
        }

        for (int i = 0; i < expected.length; i++) {
            if (header[i] != expected[i]) {
                throw new RuntimeException("INVALID_AVATAR");
            }
        }
    }
}