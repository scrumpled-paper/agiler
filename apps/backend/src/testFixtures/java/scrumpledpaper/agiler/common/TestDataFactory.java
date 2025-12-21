package scrumpledpaper.agiler.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.fixture.ImageFixture;
import scrumpledpaper.agiler.fixture.IssueFixture;
import scrumpledpaper.agiler.fixture.IssueSnapshotDateMappingFixture;
import scrumpledpaper.agiler.fixture.IssueTemplateFixture;
import scrumpledpaper.agiler.fixture.KanbanConfigFixture;
import scrumpledpaper.agiler.fixture.LabelFixture;
import scrumpledpaper.agiler.fixture.MeetingFixture;
import scrumpledpaper.agiler.fixture.MeetingTemplateFixture;
import scrumpledpaper.agiler.fixture.NotificationSubscriptionFixture;
import scrumpledpaper.agiler.fixture.ProfileFixture;
import scrumpledpaper.agiler.fixture.ProfileNotificationChannelFixture;
import scrumpledpaper.agiler.fixture.ProjectFixture;
import scrumpledpaper.agiler.fixture.RetroTemplateFixture;
import scrumpledpaper.agiler.fixture.ScheduleNotificationFixture;
import scrumpledpaper.agiler.fixture.ScrumTemplateFixture;
import scrumpledpaper.agiler.fixture.TokenFixture;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.kanban.entity.DefaultKanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.kanban.repository.IssueLabelRepository;
import scrumpledpaper.agiler.kanban.repository.IssueProfileRepository;
import scrumpledpaper.agiler.kanban.repository.IssueRepository;
import scrumpledpaper.agiler.kanban.repository.IssueSnapshotDateMappingRepository;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigRepository;
import scrumpledpaper.agiler.kanban.repository.LabelRepository;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.repository.MeetingProfileRepository;
import scrumpledpaper.agiler.note.repository.MeetingRepository;
import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.domain.NotificationSubscription;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;
import scrumpledpaper.agiler.notification.domain.ScheduledNotification;
import scrumpledpaper.agiler.notification.repository.NotificationSubscriptionRepository;
import scrumpledpaper.agiler.notification.repository.ProfileNotificationChannelRepository;
import scrumpledpaper.agiler.notification.repository.ScheduledNotificationRepository;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.project.repository.ProfileRepository;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.template.entity.IssueTemplate;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;
import scrumpledpaper.agiler.template.entity.RetroTemplate;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;
import scrumpledpaper.agiler.template.repository.IssueTemplateRepository;
import scrumpledpaper.agiler.template.repository.MeetingTemplateRepository;
import scrumpledpaper.agiler.template.repository.RetroTemplateRepository;
import scrumpledpaper.agiler.template.repository.ScrumTemplateRepository;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
	private final TokenFixture tokenFixture;
	private final UserRepository userRepository;
	private final IssueRepository issueRepository;
	private final ImageRepository imageRepository;
	private final LabelRepository labelRepository;
	private final ProfileRepository profileRepository;
	private final ProjectRepository projectRepository;
	private final MeetingRepository meetingRepository;
	private final IssueLabelRepository issueLabelRepository;
	private final IssueProfileRepository issueProfileRepository;
	private final KanbanConfigRepository kanbanConfigRepository;
	private final IssueTemplateRepository issueTemplateRepository;
	private final ScrumTemplateRepository scrumTemplateRepository;
	private final RetroTemplateRepository retroTemplateRepository;
	private final MeetingProfileRepository meetingProfileRepository;
	private final MeetingTemplateRepository meetingTemplateRepository;
	private final NotificationSubscriptionRepository notificationSubscriptionRepository;
	private final IssueSnapshotDateMappingRepository issueSnapshotDateMappingRepository;
	private final ProfileNotificationChannelRepository profileNotificationChannelRepository;
	private final ScheduledNotificationRepository scheduledNotificationRepository;
	private final EntityManager entityManager;

	public static final long DEFAULT_IMAGE_ID = 1L;
	public static String randomString(int length) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < length) {
			sb.append(UUID.randomUUID().toString().replace("-", ""));
		}
		return sb.substring(0, length);
	}

	public Image createDefaultImage() {
		Image image = ImageFixture.createImage();
		return imageRepository.save(image);
	}

	public Image createImage(String imageUrl, String objectKey) {
		Image image = ImageFixture.createImage(imageUrl, objectKey);
		return imageRepository.save(image);
	}

	public void setUserImage(User user, Image image) {
		user.updateImageId(image.getId());
		userRepository.save(user);
	}

	public String createNotAllowedAccessToken() {
		return tokenFixture.createNotAllowedAccessToken();
	}

	public String createAccessToken(User user) {
		return tokenFixture.createAccessToken(user);
	}

	public AuthContext createAuth(Image defaultImage) {
		User user = UserFixture.createUser(defaultImage);
		userRepository.save(user);
		String token = tokenFixture.createAccessToken(user);
		return new AuthContext(user, token);
	}

	public User createUser(long imageId) {
		User user = UserFixture.createUser(imageId);
		return userRepository.save(user);
	}

	public Project createProject(String url) {
		Project project = ProjectFixture.createProject(url);
		return projectRepository.save(project);
	}

	public Project createProject() {
		Project project = ProjectFixture.createProject();
		return projectRepository.save(project);
	}

	public Project createProjectWithImageUrl(String imageUrl) {
		Image image = ImageFixture.createImage(imageUrl);
		imageRepository.save(image);
		Project project = ProjectFixture.createProject(image.getId());
		return projectRepository.save(project);
	}

	public void setProjectImage(Project project, Image image) {
		project.updateImageId(image.getId());
		projectRepository.save(project);
	}

	public void setProfileImage(Profile profile, Image image) {
		profile.updateImageId(image.getId());
		profileRepository.save(profile);
	}

	public Project createProjectAndOwnerProfile(String url, User user) {
		Project project = ProjectFixture.createProject(url);
		projectRepository.save(project);
		Profile profile = ProfileFixture.createProfile(user, project, Role.OWNER);
		profileRepository.save(profile);
		return project;
	}

	public List<Project> createProjects(User user, String urlPrefix, int count) {
		List<Project> projects = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			Project project = createProjectAndOwnerProfile(
					urlPrefix + "_" + i,
					user
			);
			projects.add(project);
		}
		return projects;
	}

	public Profile createProfileWithTime(User user, Project project, Role role, LocalDateTime createdAt) {
		Profile profile = ProfileFixture.createProfile(user, project, role);
		Profile savedProfile = profileRepository.saveAndFlush(profile);

		updateTimestamps("profile", savedProfile.getId(), createdAt);

		entityManager.flush();
		entityManager.clear();

		return profileRepository.findById(savedProfile.getId()).orElseThrow();
	}


	public Project createProjectWithTime(String url, User user, LocalDateTime createdAt) {
		Project project = ProjectFixture.createProject(url);
		Project savedProject = projectRepository.saveAndFlush(project);
		updateTimestamps("project", savedProject.getId(), createdAt);

		Profile profile = ProfileFixture.createProfile(user, savedProject, Role.OWNER);
		Profile savedProfile = profileRepository.saveAndFlush(profile);
		updateTimestamps("profile", savedProfile.getId(), createdAt);

		entityManager.flush();
		entityManager.clear();

		return projectRepository.findById(savedProject.getId()).orElseThrow();
	}

	public void updateTimestamps(String tableName, Long id, LocalDateTime createdAt) {
		entityManager.createNativeQuery(
						String.format("UPDATE %s SET created_at = :createdAt, updated_at = :createdAt WHERE id = :id", tableName))
				.setParameter("createdAt", createdAt)
				.setParameter("id", id)
				.executeUpdate();

		entityManager.flush();
		entityManager.clear();
	}

	public Project findProjectById(Long id) {
		return projectRepository.findById(id).orElseThrow();
	}

	public Profile findProfileByUserIdAndProjectId(Long userId, Long projectId) {
		return profileRepository.findByUserIdAndProjectId(userId, projectId).orElseThrow();
	}

	public Image findImageById(Long id) {
		return imageRepository.findById(id).orElseThrow();
	}

	public Profile createProfile(User user, Project project, Role role) {
		Profile profile = ProfileFixture.createProfile(user, project, role);
		return profileRepository.save(profile);
	}

	public List<Label> findLabelsByProjectId(Long projectId) {
		return labelRepository.findAllByProjectId(projectId);
	}

	public Label createLabel(Project project, String name, String description, String color) {
		Label label = LabelFixture.createLabel(project, name, color, description);
		return labelRepository.save(label);
	}

	public Label findLabelById(Long id) {
		return labelRepository.findById(id).orElseThrow();
	}

	public List<IssueTemplate> findIssueTemplatesByProjectId(Long projectId) {
		return issueTemplateRepository.findByProjectId(projectId);
	}

	public IssueTemplate createIssueTemplate(Project project, String title, String description, String contents) {
		IssueTemplate issueTemplate = IssueTemplateFixture.createIssueTemplate(
				project,
				title,
				description,
				contents
		);
		return issueTemplateRepository.save(issueTemplate);
	}

	public IssueTemplate findIssueTemplateById(Long id) {
		return issueTemplateRepository.findById(id).orElseThrow();
	}

	public List<ScrumTemplate> findScrumTemplatesByProjectId(Long projectId) {
		return scrumTemplateRepository.findByProjectId(projectId);
	}

	public ScrumTemplate createScrumTemplate(Project project, String title, String description, String contents) {
		ScrumTemplate scrumTemplate = ScrumTemplateFixture.createScrumTemplate(
				project,
				title,
				description,
				contents
		);
		return scrumTemplateRepository.save(scrumTemplate);
	}

	public ScrumTemplate findScrumTemplateById(Long id) {
		return scrumTemplateRepository.findById(id).orElseThrow();
	}

	public List<RetroTemplate> findRetroTemplatesByProjectId(Long projectId) {
		return retroTemplateRepository.findByProjectId(projectId);
	}

	public RetroTemplate createRetroTemplate(Project project, String title, String description, String contents) {
		RetroTemplate retroTemplate = RetroTemplateFixture.createRetroTemplate(
				project,
				title,
				description,
				contents
		);
		return retroTemplateRepository.save(retroTemplate);
	}

	public RetroTemplate findRetroTemplateById(Long id) {
		return retroTemplateRepository.findById(id).orElseThrow();
	}

	public List<MeetingTemplate> findMeetingTemplatesByProjectId(Long projectId) {
		return meetingTemplateRepository.findByProjectId(projectId);
	}

	public MeetingTemplate createMeetingTemplate(Project project) {
		MeetingTemplate meetingTemplate = MeetingTemplateFixture.createMeetingTemplate(
				project,
				randomString(10),
				randomString(20),
				randomString(50)
		);
		return meetingTemplateRepository.save(meetingTemplate);
	}

	public MeetingTemplate findMeetingTemplateById(Long id) {
		return meetingTemplateRepository.findById(id).orElseThrow();
	}

	public Issue findIssueByProjectId(Long projectId) {
		return issueRepository.findByProjectId(projectId).orElseThrow();
	}

	public List<IssueLabel> findIssueLabelsByIssueId(Long issueId) {
		return issueLabelRepository.findAllByIssueId(issueId);
	}

	public Issue createIssue(Project project, KanbanConfig kanbanConfig, List<Profile> assignees, List<Label> labels, Boolean isDone, LocalDateTime startedAt, LocalDateTime dueAt) {
		Issue issue = IssueFixture.createIssue(project, kanbanConfig, isDone, startedAt, dueAt);
		issue = issueRepository.saveAndFlush(issue);
		List<IssueProfile> issueProfiles = IssueFixture.createIssueProfiles(issue, assignees);
		issueProfileRepository.saveAll(issueProfiles);
		List<IssueLabel> issueLabels = IssueFixture.createIssueLabels(issue, labels);
		issueLabelRepository.saveAll(issueLabels);
		return issue;
	}

	public KanbanConfig createKanbanConfig(Project project, int priority, boolean defaultStatus, boolean backlog, Boolean isDone) {
		KanbanConfig kanbanConfig = KanbanConfigFixture.create(project, randomString(20), priority, defaultStatus, backlog, isDone);
		return kanbanConfigRepository.save(kanbanConfig);
	}

	public Optional<Issue> findIssueById(Long id) {
		return issueRepository.findById(id);
	}

	public KanbanConfig findKanbanConfigById(Long id) {
		return kanbanConfigRepository.findById(id).orElseThrow();
	}

	public List<IssueProfile> findIssueProfilesByIssueId(Long id) {
		return issueProfileRepository.findAllByIssueId(id);
	}

	public List<ProfileNotificationChannel> getAllProfileNotificationChannels(long profileId) {
		return profileNotificationChannelRepository.findByProfileId(profileId);
	}

	public ProfileNotificationChannel createProfileNotificationChannel(User user, Profile profile, String channelType, String webhookUrl) {
		ProfileNotificationChannel channel = ProfileNotificationChannelFixture.create(user.getId(), profile.getId(), ChannelType.valueOf(channelType), webhookUrl);
		return profileNotificationChannelRepository.save(channel);
	}

	public KanbanConfig createKanbanConfig(Project project, String statusName, int priority, boolean defaultStatus, boolean backlog, Boolean isDone) {
		KanbanConfig kanbanConfig = KanbanConfigFixture.create(project, statusName, priority, defaultStatus, backlog, isDone);
		return kanbanConfigRepository.save(kanbanConfig);
	}

	public List<NotificationSubscription> findNotificationSubscriptionsByProfileId(long profileId) {
		return notificationSubscriptionRepository.findByProfileId(profileId);
	}

	public NotificationSubscription createNotificationSubscription(User user, Profile profile, Issue issue, long fromKanbanConfigId, long toKanbanConfigId) {
		NotificationSubscription subscription = NotificationSubscriptionFixture.create(user.getId(), profile.getId(), issue.getId(), fromKanbanConfigId, toKanbanConfigId);
		return notificationSubscriptionRepository.save(subscription);
	}

	public List<ScheduledNotification> findScheduledNotificationByProfileId(long profileId) {
		return scheduledNotificationRepository.findByProfileId(profileId);
	}

	public ScheduledNotification createScheduledNotification(User user, Profile profile, Issue issue, LocalDateTime notificationTime, String message) {
		ScheduledNotification scheduledNotification = ScheduleNotificationFixture.create(user.getId(), profile.getId(), issue.getId(), message, notificationTime);
		return scheduledNotificationRepository.save(scheduledNotification);
	}

	public List<KanbanConfig> getKanbanConfigsByProject(Project project) {
		return kanbanConfigRepository.findAllByProjectId(project.getId());
	}

	public List<KanbanConfig> createKanbanConfigs(Project project, int count) {
		List<KanbanConfig> kanbanConfigs = new ArrayList<>();

		for (int i = 1; i <= count; i++) {
			boolean backlog = (i == 1);
			boolean defaultStatus = (i == 2);
			boolean isDone = (i == 3);

			KanbanConfig kanbanConfig = KanbanConfigFixture.create(
				project,
				randomString(10),
				i,
				defaultStatus,
				backlog,
				isDone
			);
			kanbanConfigs.add(kanbanConfig);
		}
		return kanbanConfigRepository.saveAll(kanbanConfigs);
	}

	public List<KanbanConfig> defaultKanbanConfigSet(Project project) {
		DefaultKanbanConfig[] defaults = DefaultKanbanConfig.values();
		List<KanbanConfig> kanbanConfigs = new ArrayList<>();
		for (DefaultKanbanConfig def : defaults) {
			KanbanConfig kanbanConfig = KanbanConfigFixture.create(
				project,
				def.getStatusName(),
				def.getPriority(),
				def.isDefaultStatus(),
				def.isBacklog(),
				def.isDone()
			);
			kanbanConfigs.add(kanbanConfig);
		}
		kanbanConfigs = kanbanConfigRepository.saveAllAndFlush(kanbanConfigs);
		return kanbanConfigs;
	}

	public List<KanbanConfig> findKanbanConfigsByProjectId(Long id) {
		return kanbanConfigRepository.findAllByProjectId(id);
	}

	public IssueSnapshotDateMapping createIssueSnapshotDateMapping(Project project, int issueCount,
		LocalDate snapshotDate) {
		IssueSnapshotDateMapping mapping = IssueSnapshotDateMappingFixture.createIssueSnapshotDateMapping(
			project,
			issueCount,
			snapshotDate
		);
		return issueSnapshotDateMappingRepository.save(mapping);
	}

	public IssueSnapshotDateMapping findIssueSnapshotDateMapping(Project project, LocalDate snapshotDate) {
		return issueSnapshotDateMappingRepository.findByProjectIdAndSnapshotDate(project.getId(), snapshotDate).orElse(null);
	}
	public List<Issue> findIssuesByProjectId(Long id) {
		return issueRepository.findAllByProjectId(id);
	}

	public void createMeetingWithParticipants(Project project, List<Profile> participants) {
		Meeting savedMeeting = meetingRepository.save(MeetingFixture.createMeeting(project));
		meetingProfileRepository.saveAll(
			MeetingFixture.createMeetingProfiles(savedMeeting, participants)
		);
	}

	public Page<Meeting> findMeetingsByProjectIdPaged(Long id, int page, int size) {
		Pageable pageable = Pageable.ofSize(size).withPage(page);
		return meetingRepository.findAllByProjectId(id, pageable);
	}

	public Meeting findByLatestMeetingByProjectId(Long projectId) {
		return meetingRepository.findTopByProjectIdOrderByCreatedAtDesc(projectId).orElseThrow();
	}

	public Meeting createMeeting(Project project) {
		Meeting meeting = MeetingFixture.createMeeting(project);
		return meetingRepository.save(meeting);
	}

	public Meeting findMeetingById(Long id) {
		return meetingRepository.findById(id).orElse(null);
	}
}
