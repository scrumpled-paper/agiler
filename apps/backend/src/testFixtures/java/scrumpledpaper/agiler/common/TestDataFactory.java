package scrumpledpaper.agiler.common;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import scrumpledpaper.agiler.fixture.*;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.kanban.repository.IssueRepository;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigRepository;
import scrumpledpaper.agiler.kanban.repository.LabelRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
	private final TokenFixture tokenFixture;
	private final UserRepository userRepository;
	private final ImageRepository imageRepository;
	private final LabelRepository labelRepository;
	private final ProfileRepository profileRepository;
	private final ProjectRepository projectRepository;
	private final IssueTemplateRepository issueTemplateRepository;
	private final ScrumTemplateRepository scrumTemplateRepository;
	private final RetroTemplateRepository retroTemplateRepository;
	private final MeetingTemplateRepository meetingTemplateRepository;
	private final NotificationSubscriptionRepository notificationSubscriptionRepository;
	private final ProfileNotificationChannelRepository profileNotificationChannelRepository;
	private final ScheduledNotificationRepository scheduledNotificationRepository;
	private final IssueRepository issueRepository;
	private final KanbanConfigRepository kanbanConfigRepository;
	private final EntityManager entityManager;

	public Image createDefaultImage() {
		Image image = ImageFixture.createImage();
		return imageRepository.save(image);
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

	private void updateTimestamps(String tableName, Long id, LocalDateTime createdAt) {
		entityManager.createNativeQuery(
						String.format("UPDATE %s SET created_at = :createdAt, updated_at = :createdAt WHERE id = :id", tableName))
				.setParameter("createdAt", createdAt)
				.setParameter("id", id)
				.executeUpdate();
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
		return labelRepository.findByProjectId(projectId);
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

	public MeetingTemplate createMeetingTemplate(Project project, String title, String description, String contents) {
		MeetingTemplate meetingTemplate = MeetingTemplateFixture.createMeetingTemplate(
				project,
				title,
				description,
				contents
		);
		return meetingTemplateRepository.save(meetingTemplate);
	}

	public MeetingTemplate findMeetingTemplateById(Long id) {
		return meetingTemplateRepository.findById(id).orElseThrow();
	}

	public List<ProfileNotificationChannel> getALlProfileNotificationChannels(long profileId) {
		return profileNotificationChannelRepository.findByProfileId(profileId);
	}

	public ProfileNotificationChannel createProfileNotificationChannel(User user, Profile profile, String channelType, String webhookUrl) {
		ProfileNotificationChannel channel = ProfileNotificationChannelFixture.create(user.getId(), profile.getId(), ChannelType.valueOf(channelType), webhookUrl);
		return profileNotificationChannelRepository.save(channel);
	}

	public Issue createIssue(KanbanConfig kanbanConfig, Profile profile, String title, Boolean isDone, String contents, LocalDateTime startedAt, LocalDateTime dueAt) {
		Issue issue = IssueFixture.createIssue(kanbanConfig, profile, title, isDone, contents, startedAt, dueAt);
		return issueRepository.save(issue);
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

	public Issue findIssueById(Long id) {
		return issueRepository.findById(id).orElseThrow();
	}

}
