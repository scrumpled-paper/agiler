package scrumpledpaper.agiler.note.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import scrumpledpaper.agiler.note.entity.ScrumProfile;

public interface ScrumProfileRepository extends JpaRepository<ScrumProfile, Long> {
	@Query("""
		SELECT sp
		FROM ScrumProfile sp
		LEFT JOIN FETCH sp.profile p
		WHERE sp.scrum.id IN :scrumIds
	""")
	List<ScrumProfile> findAllByScrumIdsWithProfile(List<Long> scrumIds);
}
