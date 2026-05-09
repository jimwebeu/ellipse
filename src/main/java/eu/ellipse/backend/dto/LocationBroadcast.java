package eu.ellipse.backend.dto;

import java.time.Instant;
import java.util.UUID;

public class LocationBroadcast {
    private UUID userId;
    private String name;
    private Double lat;
    private Double lng;
    private Instant timestamp;
    private String activityType;
    private UUID circleId;

    public LocationBroadcast(UUID userId, String name, Double lat, Double lng, Instant timestamp,
            String activityType, UUID circleId) {
        this.userId = userId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
        this.activityType = activityType;
        this.circleId = circleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getActivityType() {
        return activityType;
    }

    public UUID getCircleId() {
        return circleId;
    }
}