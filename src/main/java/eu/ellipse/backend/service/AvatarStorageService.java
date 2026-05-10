package eu.ellipse.backend.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AvatarStorageService {

    private static final Logger log = LoggerFactory.getLogger(AvatarStorageService.class);

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final int SMALL = 100;
    private static final int EXTRASMALL = 50;
    private static final float Q_ORIGINAL = 0.85f;
    private static final float Q_SMALL = 0.82f;
    private static final float Q_XS = 0.80f;

    private static final Pattern SAFE_AVATAR_ID = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|gif)$",
            Pattern.CASE_INSENSITIVE
    );

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
    private Path dirOriginal;
    private Path dirSmall;
    private Path dirExtrasmall;

    public AvatarStorageService(
            @Value("${ellipse.avatars.directory}") String avatarDirectory
    ) {
        this.avatarDirectory = avatarDirectory;
    }

    @PostConstruct
    public void init() throws IOException {
        root = Path.of(avatarDirectory).toAbsolutePath().normalize();
        dirOriginal = root.resolve("original");
        dirSmall = root.resolve("small");
        dirExtrasmall = root.resolve("extrasmall");
        Files.createDirectories(dirOriginal);
        Files.createDirectories(dirSmall);
        Files.createDirectories(dirExtrasmall);
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

        BufferedImage loaded;
        try (InputStream in = file.getInputStream()) {
            loaded = ImageIO.read(in);
        }
        if (loaded == null) {
            throw new RuntimeException("INVALID_AVATAR");
        }

        BufferedImage square = centerSquareCrop(toRgb(loaded));
        if (square.getWidth() < 1) {
            throw new RuntimeException("INVALID_AVATAR");
        }

        String filename = UUID.randomUUID() + ext;
        Path destOriginal = dirOriginal.resolve(filename).normalize();
        Path destSmall = dirSmall.resolve(filename).normalize();
        Path destXs = dirExtrasmall.resolve(filename).normalize();

        if (!destOriginal.startsWith(dirOriginal) || !destSmall.startsWith(dirSmall) || !destXs.startsWith(dirExtrasmall)) {
            throw new IOException("Invalid path");
        }

        writeCompressed(square, destOriginal, ext, Q_ORIGINAL);
        writeCompressed(scale(square, SMALL), destSmall, ext, Q_SMALL);
        writeCompressed(scale(square, EXTRASMALL), destXs, ext, Q_XS);

        return filename;
    }

    public void deleteIfExists(String avatarId) {
        if (avatarId == null || avatarId.isBlank() || !SAFE_AVATAR_ID.matcher(avatarId).matches()) {
            return;
        }
        for (Path base : new Path[]{dirOriginal, dirSmall, dirExtrasmall}) {
            Path path = base.resolve(avatarId).normalize();
            if (!path.startsWith(base)) {
                continue;
            }
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("Failed to delete avatar '{}': {}", avatarId, e.getMessage());
            }
        }
    }

    private static BufferedImage toRgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, src.getWidth(), src.getHeight());
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return rgb;
    }

    private static BufferedImage centerSquareCrop(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int side = Math.min(w, h);
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        return src.getSubimage(x, y, side, side);
    }

    private static BufferedImage scale(BufferedImage src, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, size, size, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    private static void writeCompressed(BufferedImage img, Path dest, String ext, float quality) throws IOException {
        String format = switch (ext) {
            case ".jpg" -> "jpg";
            case ".png" -> "png";
            case ".gif" -> "gif";
            default -> throw new IOException("Unsupported image format");
        };

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new IOException("No image writer for " + format);
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(Files.newOutputStream(dest))) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
    }

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
