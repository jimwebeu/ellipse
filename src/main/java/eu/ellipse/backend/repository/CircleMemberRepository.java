package eu.ellipse.backend.repository;

import eu.ellipse.backend.model.CircleMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CircleMemberRepository extends JpaRepository<CircleMember, UUID> {

    List<CircleMember> findAllByCircleId(UUID circleId);

    List<CircleMember> findAllByUserId(UUID userId);

    Optional<CircleMember> findByCircleIdAndUserId(UUID circleId, UUID userId);

    boolean existsByCircleIdAndUserId(UUID circleId, UUID userId);

    void deleteAllByUserId(UUID userId);

    void deleteAllByCircleId(UUID circleId);
}