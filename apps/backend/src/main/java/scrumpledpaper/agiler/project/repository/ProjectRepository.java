package scrumpledpaper.agiler.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	boolean existsByUrl(String url);

	Optional<Project> findByUrl(String url);
}
