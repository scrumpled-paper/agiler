package scrumpledpaper.agiler.kanban.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
public interface IssueSnapshotDateMappingRepository extends JpaRepository<IssueSnapshotDateMapping, Long> {
}
