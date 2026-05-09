package eu.ellipse.backend.security;

import eu.ellipse.backend.model.User;
import eu.ellipse.backend.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenVersionCache {

    private final Map<UUID, Integer> cache = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public TokenVersionCache(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isValid(UUID userId, Integer tokenVersion) {
        Integer cachedVersion = cache.get(userId);

        if (cachedVersion != null) {
            return cachedVersion.equals(tokenVersion);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        cache.put(userId, user.getTokenVersion());
        return user.getTokenVersion().equals(tokenVersion);
    }

    public void invalidate(UUID userId) {
        cache.remove(userId);
    }
}