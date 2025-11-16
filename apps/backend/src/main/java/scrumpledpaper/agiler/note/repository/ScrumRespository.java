package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.Scrum;

public interface ScrumRespository extends JpaRepository<Scrum, Long> {
}
