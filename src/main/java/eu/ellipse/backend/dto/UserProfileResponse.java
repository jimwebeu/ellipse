package eu.ellipse.backend.dto;

import eu.ellipse.backend.model.User;

import java.time.Instant;
import java.util.UUID;

public class UserProfileResponse {
    private UUID id;
    private String email;
    private String name;
    private String avatarId;
    private Instant lastSeen;

    public UserProfileResponse() {
    }

    public UserProfileResponse(UUID id, String email, String name, String avatarId, Instant lastSeen) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.avatarId = avatarId;
        this.lastSeen = lastSeen;
    }

    public static UserProfileResponse fromUser(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarId(),
                user.getLastSeen()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}
