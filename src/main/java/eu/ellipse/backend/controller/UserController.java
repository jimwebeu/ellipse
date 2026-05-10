package eu.ellipse.backend.controller;

import eu.ellipse.backend.dto.UpdateUserRequest;
import eu.ellipse.backend.dto.UserProfileResponse;
import eu.ellipse.backend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe() {
        return ResponseEntity.ok(userService.getProfile(currentUserId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> patchMe(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateProfile(currentUserId(), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(currentUserId(), file));
    }

    private static UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) auth.getPrincipal();
    }
}
