package eu.ellipse.backend.service;

import eu.ellipse.backend.dto.AuthResponse;
import eu.ellipse.backend.dto.LoginRequest;
import eu.ellipse.backend.dto.RegisterRequest;
import eu.ellipse.backend.exception.InvalidCredentialsException;
import eu.ellipse.backend.model.User;
import eu.ellipse.backend.repository.UserRepository;
import eu.ellipse.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CircleService circleService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       CircleService circleService
                    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.circleService = circleService;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("INVALID_CREDENTIALS");
        }

        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getTokenVersion());
        return new AuthResponse(token, user.getId());
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_USED");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        user.setTokenVersion(0);

        userRepository.save(user);

        // gonna change in the future for the default private param to be true
        circleService.createCircle(user.getId(), user.getName() + "'s Circle", false);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getTokenVersion());
        return new AuthResponse(token, user.getId());
    }
}