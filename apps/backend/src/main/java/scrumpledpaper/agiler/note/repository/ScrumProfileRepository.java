package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.ScrumProfile;

public interface ScrumProfileRepository extends JpaRepository<ScrumProfile, Long> {
}
