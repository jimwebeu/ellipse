package eu.ellipse.backend.dto;

import java.time.Instant;
import java.util.UUID;

public class CircleMemberWithUserResponse {
    private UUID memberId;
    private UUID circleId;
    private String role;
    private Instant joinedAt;
    private UUID userId;
    private String name;
    private String email;
    private String avatarId;
    private Instant lastSeen;

    public CircleMemberWithUserResponse(
            UUID memberId,
            UUID circleId,
            String role,
            Instant joinedAt,
            UUID userId,
            String name,
            String email,
            String avatarId,
            Instant lastSeen
    ) {
        this.memberId = memberId;
        this.circleId = circleId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.avatarId = avatarId;
        this.lastSeen = lastSeen;
    }

    public UUID getMemberId() { return memberId; }
    public UUID getCircleId() { return circleId; }
    public String getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatarId() { return avatarId; }
    public Instant getLastSeen() { return lastSeen; }
}
