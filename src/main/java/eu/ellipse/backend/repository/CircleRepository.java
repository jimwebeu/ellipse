package eu.ellipse.backend.repository;

import eu.ellipse.backend.model.Circle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CircleRepository extends JpaRepository<Circle, UUID> {

    List<Circle> findAllByOwnerId(UUID ownerId);

    Optional<Circle> findByInviteCode(String inviteCode);

    void deleteAllByOwnerId(UUID ownerId);
}