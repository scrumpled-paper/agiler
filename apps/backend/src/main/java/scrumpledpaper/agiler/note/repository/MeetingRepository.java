package scrumpledpaper.agiler.note.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
	Page<Meeting> findAllByProjectId(Long projectId, Pageable pageable);

	Optional<Meeting> findTopByProjectIdOrderByCreatedAtDesc(Long projectId);
}
