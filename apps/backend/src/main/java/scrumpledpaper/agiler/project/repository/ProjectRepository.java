package scrumpledpaper.agiler.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
