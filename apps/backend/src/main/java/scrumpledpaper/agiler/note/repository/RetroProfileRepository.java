package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.note.entity.RetroProfile;

public interface RetroProfileRepository extends JpaRepository<RetroProfile, Long> {
}
