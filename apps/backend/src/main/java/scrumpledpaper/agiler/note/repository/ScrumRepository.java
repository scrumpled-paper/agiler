package scrumpledpaper.agiler.note.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.Scrum;

public interface ScrumRepository extends JpaRepository<Scrum, Long> {
	Page<Scrum> findAllByProjectId(Long id, Pageable pageable);

	Optional<Scrum> findTopByProjectIdOrderByCreatedAtDesc(Long id);

	Optional<Scrum> findByIdAndProjectId(long scrumId, Long id);

	boolean existsByIdAndProjectId(Long scrumId, Long projectId);
}
