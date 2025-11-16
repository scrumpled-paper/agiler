package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.note.entity.Retro;

public interface RetroRepository extends JpaRepository<Retro, Long> {
}
