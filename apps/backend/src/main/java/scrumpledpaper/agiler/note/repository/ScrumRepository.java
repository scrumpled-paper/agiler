package scrumpledpaper.agiler.note.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.Scrum;

public interface ScrumRepository extends JpaRepository<Scrum, Long> {
	Page<Scrum> findAllByProjectId(Long id, Pageable pageable);
}
