package scrumpledpaper.agiler.fixture;

import java.time.LocalDate;

import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.project.entity.Project;

public class IssueSnapshotDateMappingFixture {

	public static IssueSnapshotDateMapping createIssueSnapshotDateMapping(Project project, int issueCount, LocalDate snapshotDate) {
		return IssueSnapshotDateMapping.builder()
			.project(project)
			.issueCount(issueCount)
			.snapshotDate(snapshotDate)
			.build();
	}
}
