package scrumpledpaper.agiler.note.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.note.entity.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
	Page<Meeting> findAllByProjectId(Long projectId, Pageable pageable);
}
