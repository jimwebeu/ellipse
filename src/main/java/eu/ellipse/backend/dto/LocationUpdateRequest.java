package eu.ellipse.backend.dto;

import java.util.UUID;

public class LocationUpdateRequest {
    private UUID circleId;
    private Double lat;
    private Double lng;
    private String activityType;

    public UUID getCircleId() { return circleId; }
    public void setCircleId(UUID circleId) { this.circleId = circleId; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
}