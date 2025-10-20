package scrumpledpaper.agiler.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.user.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
	Optional<Profile> findByUserIdAndProjectId(Long userId, Long projectId);
}
