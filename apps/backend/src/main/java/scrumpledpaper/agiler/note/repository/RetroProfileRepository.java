package scrumpledpaper.agiler.note.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import scrumpledpaper.agiler.note.entity.RetroProfile;

public interface RetroProfileRepository extends JpaRepository<RetroProfile, Long> {
	@Query("""
		SELECT rp
		FROM RetroProfile rp
		LEFT JOIN FETCH rp.profile p
		WHERE rp.retro.id IN :retroIds
	""")
	List<RetroProfile> findAllByRetroIdsWithProfile(List<Long> retroIds);

	List<RetroProfile> findAllByRetroId(Long id);

	@Query("""
		SELECT rp
		FROM RetroProfile rp
		LEFT JOIN FETCH rp.profile p
		WHERE rp.retro.id = :id
	""")
	List<RetroProfile> findAllByRetroIdWithProfile(Long id);
}
