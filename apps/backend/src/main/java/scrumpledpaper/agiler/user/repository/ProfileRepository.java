package scrumpledpaper.agiler.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import scrumpledpaper.agiler.user.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
	Optional<Profile> findByUserIdAndProjectId(Long userId, Long projectId);

	Page<Profile> findByUserId(Long userId, Pageable pageable);
}
