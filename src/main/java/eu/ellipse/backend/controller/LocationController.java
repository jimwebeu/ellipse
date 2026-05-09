package eu.ellipse.backend.controller;

import eu.ellipse.backend.dto.LocationUpdateRequest;
import eu.ellipse.backend.security.JwtUtil;
import eu.ellipse.backend.security.TokenVersionCache;
import eu.ellipse.backend.service.LocationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class LocationController {

    private final LocationService locationService;
    private final JwtUtil jwtUtil;
    private final TokenVersionCache tokenVersionCache;

    public LocationController(LocationService locationService,
                               JwtUtil jwtUtil,
                               TokenVersionCache tokenVersionCache) {
        this.locationService = locationService;
        this.jwtUtil = jwtUtil;
        this.tokenVersionCache = tokenVersionCache;
    }

    @MessageMapping("/location/update")
    public void updateLocation(LocationUpdateRequest request, StompHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        String token = (String) accessor.getSessionAttributes().get("token");

        UUID userUUID = UUID.fromString(userId);
        Integer tokenVersion = jwtUtil.extractTokenVersion(token);

        if (!tokenVersionCache.isValid(userUUID, tokenVersion)) {
            throw new RuntimeException("UNAUTHORIZED");
        }

        locationService.handleLocationUpdate(userUUID, request);
    }
}