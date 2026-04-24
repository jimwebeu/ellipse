package eu.ellipse.backend.service;

import eu.ellipse.backend.model.Circle;
import eu.ellipse.backend.model.CircleMember;
import eu.ellipse.backend.repository.CircleMemberRepository;
import eu.ellipse.backend.repository.CircleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CircleService {

    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;

    public CircleService(CircleRepository circleRepository,
            CircleMemberRepository circleMemberRepository) {
        this.circleRepository = circleRepository;
        this.circleMemberRepository = circleMemberRepository;
    }

    public Circle createCircle(UUID ownerId, String name, boolean isPrivate) {
        Circle circle = new Circle();
        circle.setName(name);
        circle.setOwnerId(ownerId);
        circle.setInviteCode(generateInviteCode());
        circle.setIsInviteCodeEnabled(!isPrivate);
        circle.setCreatedAt(LocalDateTime.now());
        circleRepository.save(circle);

        CircleMember member = new CircleMember();
        member.setCircleId(circle.getId());
        member.setUserId(ownerId);
        member.setRole("OWNER");
        member.setJoinedAt(LocalDateTime.now());
        circleMemberRepository.save(member);

        return circle;
    }

    public CircleMember joinCircle(UUID userId, String inviteCode) {
        Circle circle = circleRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("CIRCLE_NOT_FOUND"));

        if (!circle.getIsInviteCodeEnabled()) {
            throw new RuntimeException("INVITE_DISABLED");
        }

        if (circleMemberRepository.existsByCircleIdAndUserId(circle.getId(), userId)) {
            throw new RuntimeException("ALREADY_MEMBER");
        }

        CircleMember member = new CircleMember();
        member.setCircleId(circle.getId());
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(LocalDateTime.now());
        circleMemberRepository.save(member);

        return member;
    }

    public List<CircleMember> getCircleMembers(UUID circleId, UUID requestingUserId) {
        if (!circleMemberRepository.existsByCircleIdAndUserId(circleId, requestingUserId)) {
            throw new RuntimeException("INVALID_PERMISSION");
        }

        return circleMemberRepository.findAllByCircleId(circleId);
    }

    public List<Circle> getUserCircles(UUID userId) {
        List<CircleMember> memberships = circleMemberRepository.findAllByUserId(userId);
        return memberships.stream()
                .map(m -> circleRepository.findById(m.getCircleId()).orElseThrow())
                .toList();
    }

    public void leaveCircle(UUID userId, UUID circleId) {
        CircleMember member = circleMemberRepository
                .findByCircleIdAndUserId(circleId, userId)
                .orElseThrow(() -> new RuntimeException("INVALID_PERMISSION"));

        if (member.getRole().equals("OWNER")) {
            throw new RuntimeException("INVALID_OPERATION");
        }

        circleMemberRepository.delete(member);
    }

    public void deleteCircle(UUID circleId, UUID requestingUserId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new RuntimeException("CIRCLE_NOT_FOUND"));

        if (!circle.getOwnerId().equals(requestingUserId)) {
            throw new RuntimeException("INVALID_PERMISSION");
        }

        circleMemberRepository.deleteAllByCircleId(circleId);
        circleRepository.delete(circle);
    }

    public Circle toggleInviteCode(UUID circleId, UUID requestingUserId) {
        Circle circle = circleRepository.findById(circleId)
                .orElseThrow(() -> new RuntimeException("CIRCLE_NOT_FOUND"));

        if (!circle.getOwnerId().equals(requestingUserId)) {
            throw new RuntimeException("INVALID_PERMISSION");
        }

        circle.setIsInviteCodeEnabled(!circle.getIsInviteCodeEnabled());
        return circleRepository.save(circle);
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }
}