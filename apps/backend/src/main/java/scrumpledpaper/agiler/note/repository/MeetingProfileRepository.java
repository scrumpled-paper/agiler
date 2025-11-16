package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.MeetingProfile;

public interface MeetingProfileRepository extends JpaRepository<MeetingProfile, Long> {
}
