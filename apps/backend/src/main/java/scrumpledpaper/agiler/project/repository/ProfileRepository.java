package scrumpledpaper.agiler.project.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Role;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
	Optional<Profile> findByUserIdAndProjectId(Long userId, Long projectId);

	Page<Profile> findByUserId(Long userId, Pageable pageable);

	boolean existsByUserIdAndProjectId(Long userId, Long projectId);

	Page<Profile> findByProjectId(Long projectId, Pageable pageable);

	Optional<Profile> findByIdAndProjectId(Long profileId, Long projectId);

	long countByProjectIdAndRoleAndIdNot(Long projectId, Role role, Long excludeProfileId);
}
