package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.note.entity.NoteProfile;

public interface NoteProfileRepository extends JpaRepository<NoteProfile, Long> {
}
