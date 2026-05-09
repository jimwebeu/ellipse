package eu.ellipse.backend.repository;

import eu.ellipse.backend.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, UUID> {
    List<UserLocation> findAllByUserIdIn(List<UUID> userIds);
}