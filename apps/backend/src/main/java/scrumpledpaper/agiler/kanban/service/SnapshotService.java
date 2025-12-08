package scrumpledpaper.agiler.kanban.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.mapper.IssueMapper;
import scrumpledpaper.agiler.kanban.repository.IssueSnapshotDateMappingRepository;
import scrumpledpaper.agiler.project.entity.Project;

@Service
@RequiredArgsConstructor
public class SnapshotService {
	private final IssueSnapshotDateMappingRepository issueSnapshotDateMappingRepository;
	private final IssueMapper issueMapper;

	public void saveSnapshotMapping(Project project, LocalDate snapshotDate, int count) {
		issueSnapshotDateMappingRepository.save(issueMapper.toIssueSnapshotDateMapping(project, snapshotDate, count));
	}

	public Optional<IssueSnapshotDateMapping> findByProjectIdAndSnapshotDate(Long projectId, LocalDate snapshotDate) {
		return issueSnapshotDateMappingRepository.findByProjectIdAndSnapshotDate(projectId, snapshotDate);
	}

	public LocalDate getSnapshotDateForToday(LocalDateTime time, int issueSnapshotStartHour) {
		LocalDate snapshotDate;
		if (time.getHour() < issueSnapshotStartHour) {
			// ISSUE_SNAPSHOT_START_HOUR 이전에 실행된 경우
			// 전전날 ISSUE_SNAPSHOT_START_HOUR 이후부터 전날 ISSUE_SNAPSHOT_START_HOUR 이전까지의 이슈를 조회하므로
			snapshotDate = LocalDate.now().minusDays(1); // 스냅샷 날짜는 1일 전
		} else {
			// ISSUE_SNAPSHOT_START_HOUR 이후에 실행된 경우
			// 전날 ISSUE_SNAPSHOT_START_HOUR 이후부터 오늘 ISSUE_SNAPSHOT_START_HOUR 이전까지의 이슈를 조회하므로
			snapshotDate = LocalDate.now().minusDays(0); // 스냅샷 날짜는 당일
		}
		return snapshotDate;
	}

	public void countDownIssueSnapshotMappingAndDeleteIfZero(Project project, LocalDateTime issueCreatedAt, int issueSnapshotStartHour) {
		LocalDate issueSnapshotDate = issueCreatedAt.toLocalDate();
		// ISSUE_SNAPSHOT_START_HOUR 이전에 생성된 이슈인 경우, 전날 스냅샷 날짜에서 값을 줄여야 함
		// 아닐 경우 그냥 이슈 생성일 기준으로 값을 줄이면 됨
		// 하지만 당일의 이슈(스냅샷 생성 전)인 경우는 리턴
		if (issueCreatedAt.getHour() < issueSnapshotStartHour) {
			issueSnapshotDate.minusDays(1);
		}

		Optional<IssueSnapshotDateMapping> mapping = issueSnapshotDateMappingRepository
			.findByProjectIdAndSnapshotDate(project.getId(), issueSnapshotDate);

		if (mapping.isEmpty()) {
			return;
		}
		mapping.get().decrementIssueSnapshotMappingCount();

		if (mapping.get().getIssueCount() <= 0) {
			issueSnapshotDateMappingRepository.delete(mapping.get());
		}
	}

	public List<LocalDate> issueSnapshotDateMappingsByProjectIdAndBetween(Long projectId, LocalDate startDate, LocalDate endDate) {
		return issueSnapshotDateMappingRepository
			.findByProjectIdAndSnapshotDateBetweenOrderBySnapshotDateDesc(projectId, startDate, endDate)
			.stream()
			.map(IssueSnapshotDateMapping::getSnapshotDate)
			.toList();
	}
}
