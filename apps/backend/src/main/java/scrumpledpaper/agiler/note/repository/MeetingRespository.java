package scrumpledpaper.agiler.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.Meeting;

public interface MeetingRespository extends JpaRepository<Meeting, Long> {
}
