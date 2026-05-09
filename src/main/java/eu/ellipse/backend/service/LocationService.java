package eu.ellipse.backend.service;

import eu.ellipse.backend.dto.LocationBroadcast;
import eu.ellipse.backend.dto.LocationUpdateRequest;
import eu.ellipse.backend.model.LocationHistory;
import eu.ellipse.backend.model.User;
import eu.ellipse.backend.model.UserLocation;
import eu.ellipse.backend.repository.CircleMemberRepository;
import eu.ellipse.backend.repository.LocationHistoryRepository;
import eu.ellipse.backend.repository.UserLocationRepository;
import eu.ellipse.backend.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class LocationService {

    private static final double HISTORY_DISTANCE_THRESHOLD_METERS = 30.0;
    private static final long HISTORY_TIME_THRESHOLD_MINUTES = 2;

    private final UserLocationRepository userLocationRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CircleMemberRepository circleMemberRepository;

    public LocationService(UserLocationRepository userLocationRepository,
            LocationHistoryRepository locationHistoryRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            CircleMemberRepository circleMemberRepository) {
        this.userLocationRepository = userLocationRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.circleMemberRepository = circleMemberRepository;
    }

    public void handleLocationUpdate(UUID userId, LocationUpdateRequest request) {
        if (!circleMemberRepository.existsByCircleIdAndUserId(request.getCircleId(), userId)) {
            throw new RuntimeException("NOT_A_MEMBER");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        Optional<UserLocation> existingLocation = userLocationRepository.findById(userId);

        UserLocation userLocation = existingLocation.orElse(new UserLocation());
        userLocation.setUserId(userId);
        userLocation.setLat(request.getLat());
        userLocation.setLng(request.getLng());
        userLocation.setUpdatedAt(Instant.now());
        userLocation.setActivityType(request.getActivityType());
        userLocationRepository.save(userLocation);

        user.setLastSeen(Instant.now());
        userRepository.save(user);

        boolean shouldSaveHistory = false;

        if (existingLocation.isEmpty()) {
            shouldSaveHistory = true;
        } else {
            double distance = calculateDistance(
                    existingLocation.get().getLat(),
                    existingLocation.get().getLng(),
                    request.getLat(),
                    request.getLng());

            long minutesSinceLastSave = ChronoUnit.MINUTES.between(
                    existingLocation.get().getUpdatedAt(),
                    Instant.now());

            boolean movedEnough = distance > HISTORY_DISTANCE_THRESHOLD_METERS;
            boolean timeElapsed = minutesSinceLastSave >= HISTORY_TIME_THRESHOLD_MINUTES;

            shouldSaveHistory = movedEnough || timeElapsed;
        }

        if (shouldSaveHistory) {
            LocationHistory history = new LocationHistory();
            history.setUserId(userId);
            history.setCircleId(request.getCircleId());
            history.setLat(request.getLat());
            history.setLng(request.getLng());
            history.setRecordedAt(Instant.now());
            history.setActivityType(request.getActivityType());
            locationHistoryRepository.save(history);
        }

        LocationBroadcast broadcast = new LocationBroadcast(
                userId,
                user.getName(),
                request.getLat(),
                request.getLng(),
                Instant.now(),
                request.getActivityType(),
                request.getCircleId()
            );

        messagingTemplate.convertAndSend(
                "/topic/circle/" + request.getCircleId(),
                broadcast);
    }

    // Haversine formula. calculates distance between two coordinates in meters
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS_METERS = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}