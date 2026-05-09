package eu.ellipse.backend.repository;

import eu.ellipse.backend.model.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, UUID> {
    List<LocationHistory> findAllByUserIdAndRecordedAtBetween(UUID userId, Instant from, Instant to);
    List<LocationHistory> findAllByCircleIdAndRecordedAtBetween(UUID circleId, Instant from, Instant to);
    List<LocationHistory> findAllByUserId(UUID userId);
}