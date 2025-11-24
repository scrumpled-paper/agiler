package scrumpledpaper.agiler.notification;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.kanban.dto.IssueKanbanConfigReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.notification.client.DiscordAuthClient;
import scrumpledpaper.agiler.notification.client.DiscordNotificationClient;
import scrumpledpaper.agiler.notification.client.SlackAuthClient;
import scrumpledpaper.agiler.notification.client.SlackNotificationClient;
import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.domain.NotificationSubscription;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;
import scrumpledpaper.agiler.notification.domain.ScheduledNotification;
import scrumpledpaper.agiler.notification.dto.DiscordOAuthResDto;
import scrumpledpaper.agiler.notification.dto.NotificationChannelsResDto;
import scrumpledpaper.agiler.notification.dto.NotificationSubscriptionReqDto;
import scrumpledpaper.agiler.notification.dto.ScheduleNotificationReqDto;
import scrumpledpaper.agiler.notification.dto.SlackOAuthResDto;
import scrumpledpaper.agiler.notification.dto.SubscriptionsListResDto;
import scrumpledpaper.agiler.notification.service.OAuthStateService;
import scrumpledpaper.agiler.notification.service.ScheduledNotificationService;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

@IntegrationTest
@Transactional
class NotificationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	@Autowired
	private OAuthStateService oAuthStateService;
	@Autowired
	private ScheduledNotificationService scheduledNotificationService;
	@MockitoBean
	private SlackAuthClient slackAuthClient;
	@MockitoBean
	private DiscordAuthClient discordAuthClient;
	@MockitoBean
	private SlackNotificationClient slackNotificationClient;
	@MockitoBean
	private DiscordNotificationClient discordNotificationClient;

	private AuthContext auth;

	@BeforeEach
	void setUp() {
		Image image = testDataFactory.createDefaultImage();
		auth = testDataFactory.createAuth(image);
	}

	private Cookie getAuthCookie() {
		return new Cookie("accessToken", auth.getToken());
	}

	@Test
	@DisplayName("302 - Slack 연동 시작 시 Slack 인증 페이지로 리디렉션된다")
	void connectToSlack_shouldRedirectToSlackAuthPage() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/projects/" + project.getUrl() + "/notifications/slack/connect")
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", containsString("slack.com/oauth/v2/authorize")));
	}

	@Test
	@DisplayName("200 - Slack OAuth 콜백 처리 시 채널이 등록된다")
	void handleSlackCallback_shouldRegisterChannel() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		String state = oAuthStateService.createState(auth.getUser().getId(), profile.getId());

		String code = "test-slack-code";
		SlackOAuthResDto.IncomingWebhook webhook = new SlackOAuthResDto.IncomingWebhook();
		webhook.setChannel("test-channel");
		webhook.setUrl("https://hooks.slack.com/test-webhook");
		SlackOAuthResDto responseDto = new SlackOAuthResDto();
		responseDto.setOk(true);
		responseDto.setIncomingWebhook(webhook);

		Mockito.when(slackAuthClient.getAccessToken(Mockito.anyMap())).thenReturn(responseDto);

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/notifications/slack/callback")
				.param("code", code)
				.param("state", state)
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().isOk());
		List<ProfileNotificationChannel> channels = testDataFactory.getAllProfileNotificationChannels(profile.getId());
		assertThat(channels).hasSize(1);
		assertThat(channels.getFirst().getChannelType()).isEqualTo(ChannelType.SLACK);
	}

	@Test
	@DisplayName("200 - 이미 등록된 채널을 다시 등록 시도 할 경우 새 webhook URL을 업데이트 한다")
	void handleSlackCallback_withExistingChannel_shouldUpdateWebhookUrl() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		String state = oAuthStateService.createState(auth.getUser().getId(), profile.getId());

		String code = "test-slack-code";
		SlackOAuthResDto.IncomingWebhook webhook = new SlackOAuthResDto.IncomingWebhook();
		webhook.setChannel("test-channel");
		webhook.setUrl("https://hooks.slack.com/test-webhook");
		SlackOAuthResDto responseDto = new SlackOAuthResDto();
		responseDto.setOk(true);
		responseDto.setIncomingWebhook(webhook);

		Mockito.when(slackAuthClient.getAccessToken(Mockito.anyMap())).thenReturn(responseDto);

		testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "SLACK", "https://hooks.slack.com/old-webhook");

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/notifications/slack/callback")
				.param("code", code)
				.param("state", state)
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().isOk());
		List<ProfileNotificationChannel> channels = testDataFactory.getAllProfileNotificationChannels(profile.getId());
		assertThat(channels).hasSize(1);
		assertThat(channels.getFirst().getChannelType()).isEqualTo(ChannelType.SLACK);
		assertThat(channels.getFirst().getWebhookUrl()).isEqualTo("https://hooks.slack.com/test-webhook");
	}

	@Test
	@DisplayName("302 - Discord 연동 시작 시 Discord 인증 페이지로 리디렉션된다")
	void connectToDiscord_shouldRedirectToDiscordAuthPage() throws Exception {
		// when
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());

		ResultActions result = mockMvc.perform(get("/api/v1/projects/" + project.getUrl() + "/notifications/discord/connect")
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", containsString("discord.com/api/oauth2/authorize")));
	}

	@Test
	@DisplayName("200 - Discord OAuth 콜백 처리 시 채널이 등록된다")
	void handleDiscordCallback_shouldRegisterChannel() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		String state = oAuthStateService.createState(auth.getUser().getId(), profile.getId());

		String code = "test-discord-code";
		DiscordOAuthResDto.Webhook webhook = new DiscordOAuthResDto.Webhook();
		webhook.setName("test-discord-channel");
		webhook.setUrl("https://discord.com/api/webhooks/test");
		DiscordOAuthResDto responseDto = new DiscordOAuthResDto();
		responseDto.setWebhook(webhook);

		Mockito.when(discordAuthClient.getAccessToken(Mockito.anyMap())).thenReturn(responseDto);

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/notifications/discord/callback")
				.param("code", code)
				.param("state", state)
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().isOk());
		List<ProfileNotificationChannel> channels = testDataFactory.getAllProfileNotificationChannels(profile.getId());
		assertThat(channels).hasSize(1);
		assertThat(channels.getFirst().getChannelType()).isEqualTo(ChannelType.DISCORD);
	}

	@Test
	@DisplayName("200 - 사용자 알림 채널 목록을 조회한다")
	void getProfileNotificationChannels_shouldReturnChannels() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());

		testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "SLACK", "slack-channel");
		testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "DISCORD", "discord-channel");

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/projects/{projectUrl}/notifications/channels", project.getUrl())
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().isOk());
		NotificationChannelsResDto response = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), NotificationChannelsResDto.class);
		assertThat(response.channels()).hasSize(2);
	}

	@Test
	@DisplayName("204 - 알림 채널을 삭제한다")
	void deleteNotificationChannel_shouldDeleteChannel() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());

		ProfileNotificationChannel channel1 = testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "SLACK", "slack-channel");
		ProfileNotificationChannel channel2 = testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "DISCORD", "discord-channel");

		// when
		ResultActions result1 = mockMvc.perform(delete("/api/v1/projects/{projectUrl}/notifications/channels/{channelId}", project.getUrl(), channel1.getId())
				.cookie(getAuthCookie()));
		ResultActions result2 = mockMvc.perform(delete("/api/v1/projects/{projectUrl}/notifications/channels/{channelId}", project.getUrl(), channel2.getId())
				.cookie(getAuthCookie()));

		// then
		result1.andExpect(status().isNoContent());
		result2.andExpect(status().isNoContent());
		assertThat(testDataFactory.getAllProfileNotificationChannels(profile.getId())).hasSize(0);
	}

	@Test
	@DisplayName("404 - 존재하지 않는 채널 삭제시 404 에러를 반환한다")
	void deleteNotificationChannel_withInvalidId_shouldReturnNotFound() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());

		ProfileNotificationChannel channel = testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "SLACK", "slack-channel");
		long invalidChannelId = channel.getId() + 9999;

		// when
		ResultActions result = mockMvc.perform(delete("/api/v1/projects/{projectUrl}/notifications/channels/{channelId}", project.getUrl(), invalidChannelId)
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("200 - 알림을 구독한다")
	void subscribeToNotifications_shouldCreateSubscription() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig todoKanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		KanbanConfig doneKanbanConfig = testDataFactory.createKanbanConfig(project, "DONE", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(
			project,
			todoKanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);

		NotificationSubscriptionReqDto request = new NotificationSubscriptionReqDto(issue.getId(), todoKanbanConfig.getId(), doneKanbanConfig.getId());

		// when
		ResultActions result = mockMvc.perform(post("/api/v1/projects/{projectUrl}/notifications/subscriptions", project.getUrl())
				.cookie(getAuthCookie())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// then
		result.andExpect(status().isOk());
		List<NotificationSubscription> subscriptions = testDataFactory.findNotificationSubscriptionsByProfileId(profile.getId());
		assertThat(subscriptions).hasSize(1);
		assertThat(subscriptions.getFirst().getIssueId()).isEqualTo(issue.getId());
		assertThat(subscriptions.getFirst().getFromKanbanConfigId()).isEqualTo(todoKanbanConfig.getId());
		assertThat(subscriptions.getFirst().getToKanbanConfigId()).isEqualTo(doneKanbanConfig.getId());
	}

	@Test
	@DisplayName("200 - 사용자 알림 구독 목록을 조회한다")
	void getUserSubscriptions_shouldReturnSubscriptions() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig todoKanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		KanbanConfig doneKanbanConfig = testDataFactory.createKanbanConfig(project, "DONE", 2, false, false, false);
		KanbanConfig inProgressKanbanConfig = testDataFactory.createKanbanConfig(project, "IN_PROGRESS", 2, false, false, false);
		Issue issue1 = testDataFactory.createIssue(
			project,
			todoKanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);
		Issue issue2 = testDataFactory.createIssue(
			project,
			doneKanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);
		Issue issue3 = testDataFactory.createIssue(
			project,
			todoKanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);

		testDataFactory.createNotificationSubscription(auth.getUser(), profile, issue1, todoKanbanConfig.getId(), doneKanbanConfig.getId());
		testDataFactory.createNotificationSubscription(auth.getUser(), profile, issue2, inProgressKanbanConfig.getId(), doneKanbanConfig.getId());
		testDataFactory.createNotificationSubscription(auth.getUser(), profile, issue3, todoKanbanConfig.getId(), inProgressKanbanConfig.getId());

		// when
		ResultActions result = mockMvc.perform(get("/api/v1/projects/{projectUrl}/notifications/subscriptions", project.getUrl())
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().isOk());
		SubscriptionsListResDto response = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), SubscriptionsListResDto.class);
		assertThat(response.subscriptions()).hasSize(3);
	}

	@Test
	@DisplayName("204 - 알림 구독을 취소한다")
	void unsubscribeFromNotifications_shouldDeleteSubscription() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig todoKanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		KanbanConfig doneKanbanConfig = testDataFactory.createKanbanConfig(project, "DONE", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(
			project,
			todoKanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);

		NotificationSubscription subscription = testDataFactory.createNotificationSubscription(auth.getUser(), profile, issue, todoKanbanConfig.getId(), doneKanbanConfig.getId());

		// when
		ResultActions result = mockMvc.perform(delete("/api/v1/projects/{projectUrl}/notifications/subscriptions/{subscriptionId}", project.getUrl(), subscription.getId())
				.cookie(getAuthCookie()));

		// then
		result.andExpect(status().isNoContent());
		List<NotificationSubscription> subscriptions = testDataFactory.findNotificationSubscriptionsByProfileId(profile.getId());
		assertThat(subscriptions).hasSize(0);
	}

	@Test
	@DisplayName("200 - 상대 시간으로 알림을 예약한다")
	void scheduleNotification_withDelay_shouldCreateScheduledNotification() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig kanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(
			project,
			kanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);
		ScheduleNotificationReqDto request = new ScheduleNotificationReqDto(issue.getId(), 60L, "Test schedule delay");

		// when
		ResultActions result = mockMvc.perform(post("/api/v1/projects/{projectUrl}/notifications/schedule", project.getUrl())
				.cookie(getAuthCookie())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// then
		result.andExpect(status().isOk());
		List<ScheduledNotification> scheduled = testDataFactory.findScheduledNotificationByProfileId(profile.getId());
		assertThat(scheduled).hasSize(1);
		assertThat(scheduled.getFirst().getIssueId()).isEqualTo(issue.getId());
		assertThat(scheduled.getFirst().getNotificationTime()).isAfter(LocalDateTime.now().plusMinutes(59));
	}

	@ParameterizedTest
	@ValueSource(
			longs = {0L, -10L}
	)
	@DisplayName("400 - 잘못된 예약 요청 시 에러를 반환한다")
	void scheduleNotification_withInvalidRequest_shouldReturnError(long delayMin) throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		KanbanConfig kanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(
			project,
			kanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);
		ScheduleNotificationReqDto request = new ScheduleNotificationReqDto(issue.getId(), delayMin, "Test schedule delay");

		// when
		ResultActions result = mockMvc.perform(post("/api/v1/projects/{projectUrl}/notifications/schedule", project.getUrl())
				.cookie(getAuthCookie())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// then
		result.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("200 - 이슈 상태 변경으로 인한 SLACK 알림 성공")
	void changeIssueStatus_for_slack_Success() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig todoKanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		KanbanConfig doneKanbanConfig = testDataFactory.createKanbanConfig(project, "DONE", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(
			project,
			todoKanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);
		String testWebhookUrl = "https://hooks.slack.test/webhook";
		testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "SLACK", testWebhookUrl);
		testDataFactory.createNotificationSubscription(auth.getUser(), profile, issue, todoKanbanConfig.getId(), doneKanbanConfig.getId());
		IssueKanbanConfigReqDto request = new IssueKanbanConfigReqDto(doneKanbanConfig.getId());

		// when
		ResultActions result = mockMvc.perform(patch("/api/v1/projects/{projectUrl}/issues/{issueId}/kanban-config", anyProjectUrl, issue.getId())
				.cookie(getAuthCookie())
				.content(objectMapper.writeValueAsString(request))
				.contentType("application/json"));

		// then
		result.andExpect(status().isOk());
		Issue updatedIssue = testDataFactory.findIssueById(issue.getId()).get();
		assertThat(updatedIssue.getId()).isEqualTo(issue.getId());
		assertThat(updatedIssue.getKanbanConfig().getStatusName()).isEqualTo(doneKanbanConfig.getStatusName());
		verify(slackNotificationClient, times(1))
				.sendNotification(ArgumentMatchers.eq(URI.create(testWebhookUrl)), ArgumentMatchers.contains("Issue #"));
	}

	@Test
	@DisplayName("200 - 이슈 상태 변경으로 인한 DISCORD 알림 성공")
	void changeIssueStatus_for_discord_Success() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig todoKanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		KanbanConfig doneKanbanConfig = testDataFactory.createKanbanConfig(project, "DONE", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(
			project,
			todoKanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);
		String testWebhookUrl = "https://hooks.discord.test/webhook";
		testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "DISCORD", testWebhookUrl);
		testDataFactory.createNotificationSubscription(auth.getUser(), profile, issue, todoKanbanConfig.getId(), doneKanbanConfig.getId());
		IssueKanbanConfigReqDto request = new IssueKanbanConfigReqDto(doneKanbanConfig.getId());

		// when
		ResultActions result = mockMvc.perform(patch("/api/v1/projects/{projectUrl}/issues/{issueId}/kanban-config", anyProjectUrl, issue.getId())
				.cookie(getAuthCookie())
				.content(objectMapper.writeValueAsString(request))
				.contentType("application/json"));

		// then
		result.andExpect(status().isOk());
		Issue updatedIssue = testDataFactory.findIssueById(issue.getId()).get();
		assertThat(updatedIssue.getId()).isEqualTo(issue.getId());
		assertThat(updatedIssue.getKanbanConfig().getStatusName()).isEqualTo("DONE");
		verify(discordNotificationClient, times(1))
				.sendNotification(ArgumentMatchers.eq(URI.create(testWebhookUrl)), ArgumentMatchers.contains("Issue #"));
	}

	@Test
	@DisplayName("시간 예약이 지난 알림이 정상적으로 SLACK으로 발송된다")
	void processScheduledNotifications_for_slack_Success() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig kanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(
			project,
			kanbanConfig,
			Collections.emptyList(),
			Collections.emptyList(),
			false,
			null,
			null
		);
		String testWebhookUrl = "https://hooks.discord.test/webhook";
		testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "SLACK", testWebhookUrl);
		LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
		testDataFactory.createScheduledNotification(auth.getUser(), profile, issue, pastTime, "Test scheduled message");

		// when
		scheduledNotificationService.processScheduledNotifications();

		// then
		List<ScheduledNotification> scheduled = testDataFactory.findScheduledNotificationByProfileId(profile.getId());
		assertThat(scheduled).hasSize(1);
		assertThat(scheduled.getFirst().isSent()).isTrue();
		verify(slackNotificationClient, times(1))
				.sendNotification(ArgumentMatchers.eq(URI.create(testWebhookUrl)), ArgumentMatchers.contains("Test scheduled message"));
	}

}
