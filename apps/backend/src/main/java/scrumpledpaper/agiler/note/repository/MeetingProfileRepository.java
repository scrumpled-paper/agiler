package scrumpledpaper.agiler.note.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import scrumpledpaper.agiler.note.entity.MeetingProfile;

public interface MeetingProfileRepository extends JpaRepository<MeetingProfile, Long> {
	@Query("""
		SELECT mp
		FROM MeetingProfile mp
		LEFT JOIN FETCH mp.profile p
		WHERE mp.meeting.id IN :meetingIds
	""")
	List<MeetingProfile> findAllByMeetingIdsWithProfile(List<Long> meetingIds);
}
