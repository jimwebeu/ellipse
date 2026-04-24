package eu.ellipse.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "circles")
public class Circle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String inviteCode;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private Boolean isInviteCodeEnabled = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public Boolean getIsInviteCodeEnabled() { return isInviteCodeEnabled; }
    public void setIsInviteCodeEnabled(Boolean isInviteCodeEnabled) { this.isInviteCodeEnabled = isInviteCodeEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}