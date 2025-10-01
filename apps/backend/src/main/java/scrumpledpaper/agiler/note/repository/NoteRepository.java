package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.note.entity.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
