package eu.ellipse.backend.service;

import eu.ellipse.backend.dto.UpdateUserRequest;
import eu.ellipse.backend.dto.UserProfileResponse;
import eu.ellipse.backend.model.User;
import eu.ellipse.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final int MAX_NAME_LENGTH = 200;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[\\w-]{2,}$");

    private final UserRepository userRepository;
    private final AvatarStorageService avatarStorageService;

    public UserService(UserRepository userRepository, AvatarStorageService avatarStorageService) {
        this.userRepository = userRepository;
        this.avatarStorageService = avatarStorageService;
    }

    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        return UserProfileResponse.fromUser(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        boolean changed = false;

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.length() > MAX_NAME_LENGTH) {
                throw new RuntimeException("NAME_TOO_LONG");
            }
            if (!name.isEmpty() && !name.equals(user.getName())) {
                user.setName(name);
                changed = true;
            }
        }

        if (request.getEmail() != null) {
            String email = normalizeEmail(request.getEmail());
            if (!email.isEmpty()) {
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    throw new RuntimeException("INVALID_EMAIL");
                }
                if (!email.equalsIgnoreCase(user.getEmail())) {
                    if (userRepository.existsByEmail(email)) {
                        throw new RuntimeException("EMAIL_USED");
                    }
                    user.setEmail(email);
                    changed = true;
                }
            }
        }

        if (changed) {
            userRepository.save(user);
        }
        return UserProfileResponse.fromUser(user);
    }

    @Transactional
    public UserProfileResponse uploadAvatar(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("INVALID_AVATAR");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        String previousAvatarId = user.getAvatarId();
        String newAvatarId;
        try {
            newAvatarId = avatarStorageService.store(file);
        } catch (IOException e) {
            throw new RuntimeException("AVATAR_STORE_FAILED");
        }

        user.setAvatarId(newAvatarId);
        userRepository.save(user);
        avatarStorageService.deleteIfExists(previousAvatarId);
        return UserProfileResponse.fromUser(user);
    }

    private static String normalizeEmail(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase();
    }
}
