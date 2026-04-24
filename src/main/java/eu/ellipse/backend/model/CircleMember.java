package eu.ellipse.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "circle_members")
public class CircleMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID circleId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String role; // "OWNER" or "MEMBER"

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCircleId() { return circleId; }
    public void setCircleId(UUID circleId) { this.circleId = circleId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}