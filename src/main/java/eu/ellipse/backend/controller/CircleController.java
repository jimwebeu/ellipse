package eu.ellipse.backend.controller;

import eu.ellipse.backend.dto.CreateCircleRequest;
import eu.ellipse.backend.dto.JoinCircleRequest;
import eu.ellipse.backend.security.JwtUtil;
import eu.ellipse.backend.service.CircleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/circles")
public class CircleController {

    private final CircleService circleService;
    private final JwtUtil jwtUtil;

    public CircleController(CircleService circleService, JwtUtil jwtUtil) {
        this.circleService = circleService;
        this.jwtUtil = jwtUtil;
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7); // strip "Bearer "
        System.out.println(token);
        return jwtUtil.extractUserId(token);
    }

    @PostMapping // [POST] /circles
    public ResponseEntity<?> createCircle(@RequestHeader("Authorization") String authHeader,
            @RequestBody CreateCircleRequest request) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(circleService.createCircle(userId, request.getName(), false));
    }

    @GetMapping // [GET] /circles
    public ResponseEntity<?> getMyCircles(@RequestHeader("Authorization") String authHeader) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(circleService.getUserCircles(userId));
    }

    @PostMapping("/{circleId}/join")
    public ResponseEntity<?> joinCircle(@RequestHeader("Authorization") String authHeader, @RequestBody JoinCircleRequest request) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(circleService.joinCircle(userId, request.getInviteCode()));
    }

    @GetMapping("/{circleId}/members")
    public ResponseEntity<?> getMembers(@RequestHeader("Authorization") String authHeader, @PathVariable UUID circleId) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(circleService.getCircleMembers(circleId, userId));
    }

    @DeleteMapping("/{circleId}/leave")
    public ResponseEntity<?> leaveCircle(@RequestHeader("Authorization") String authHeader, @PathVariable UUID circleId) {
        UUID userId = extractUserId(authHeader);
        circleService.leaveCircle(userId, circleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{circleId}")
    public ResponseEntity<?> deleteCircle(@RequestHeader("Authorization") String authHeader, @PathVariable UUID circleId) {
        UUID userId = extractUserId(authHeader);
        circleService.deleteCircle(circleId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{circleId}/invite")
    public ResponseEntity<?> toggleInviteCode( @RequestHeader("Authorization") String authHeader, @PathVariable UUID circleId) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(circleService.toggleInviteCode(circleId, userId));
    }
}