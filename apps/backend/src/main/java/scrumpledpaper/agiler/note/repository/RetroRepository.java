package scrumpledpaper.agiler.note.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.note.entity.Retro;

public interface RetroRepository extends JpaRepository<Retro, Long> {
	Page<Retro> findAllByProjectId(Long id, Pageable pageable);

	Optional<Retro> findTopByProjectIdOrderByCreatedAtDesc(Long id);
}
