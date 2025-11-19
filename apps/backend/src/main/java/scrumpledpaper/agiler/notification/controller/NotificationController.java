package scrumpledpaper.agiler.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.domain.NotificationSubscription;
import scrumpledpaper.agiler.notification.dto.NotificationChannelsResDto;
import scrumpledpaper.agiler.notification.dto.NotificationSubscriptionReqDto;
import scrumpledpaper.agiler.notification.dto.ScheduleNotificationReqDto;
import scrumpledpaper.agiler.notification.dto.SubscriptionsListResDto;
import scrumpledpaper.agiler.notification.service.ChannelOAuthService;
import scrumpledpaper.agiler.notification.service.NotificationManagementService;
import scrumpledpaper.agiler.notification.service.NotificationService;
import scrumpledpaper.agiler.notification.service.ScheduledNotificationService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationManagementService notificationManagementService;
	private final ScheduledNotificationService scheduledNotificationService;
	private final ChannelOAuthService channelOAuthService;
	private final NotificationService notificationService;

	@Operation(summary = "Slack 연동 시작", description = "사용자를 Slack OAuth 인증 페이지로 리디렉션합니다.")
	@GetMapping("/projects/{projectUrl}/notifications/slack/connect")
	public void connectToSlack(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable String projectUrl,
			HttpServletResponse response
	) throws IOException {
		String authorizationUri = notificationService.buildCallbackUri(customUserDetails.getUserId(), projectUrl, ChannelType.SLACK);
		response.sendRedirect(authorizationUri);
	}

	@Operation(summary = "Slack OAuth 콜백 처리", description = "Slack OAuth 콜백을 처리하고 알림 채널을 등록합니다.")
	@GetMapping("/notifications/slack/callback")
	public ResponseEntity<Void> handleSlackCallback(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam("code") String code,
			@RequestParam("state") String state
	) {
		channelOAuthService.processSlackCallback(code, state, userDetails.getUserId());
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Discord 연동 시작", description = "사용자를 Discord OAuth 인증 페이지로 리디렉션합니다.")
	@GetMapping("/projects/{projectUrl}/notifications/discord/connect")
	public void connectToDiscord(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable String projectUrl,
			HttpServletResponse response
	) throws IOException {
		String authorizationUri = notificationService.buildCallbackUri(customUserDetails.getUserId(), projectUrl, ChannelType.DISCORD);
		response.sendRedirect(authorizationUri);
	}

	@Operation(summary = "Discord OAuth 콜백 처리", description = "Discord OAuth 콜백을 처리하고 알림 채널을 등록합니다.")
	@GetMapping("/notifications/discord/callback")
	public ResponseEntity<Void> handleDiscordCallback(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam("code") String code,
			@RequestParam("state") String state
	) {
		channelOAuthService.processDiscordCallback(code, state, userDetails.getUserId());
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "사용자 알림 채널 조회", description = "등록된 사용자 알림 채널 목록을 조회합니다.")
	@GetMapping("/projects/{projectUrl}/notifications/channels")
	public ResponseEntity<NotificationChannelsResDto> getUserNotificationChannels(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String projectUrl
	) {
		NotificationChannelsResDto channels = notificationManagementService.getUserChannels(userDetails.getUserId(), projectUrl);
		return ResponseEntity.ok(channels);
	}

	@Operation(summary = "알림 채널 삭제", description = "사용자 알림 채널을 삭제합니다.")
	@DeleteMapping("/projects/{projectUrl}/notifications/channels/{channelId}")
	public ResponseEntity<Void> deleteNotificationChannel(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String projectUrl,
			@PathVariable Long channelId
	) {
		notificationManagementService.deleteChannel(userDetails.getUserId(), projectUrl, channelId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "알림 구독", description = "특정 이벤트에 대한 알림을 구독합니다.")
	@PostMapping("/projects/{projectUrl}/notifications/subscriptions")
	public ResponseEntity<NotificationSubscription> subscribeToNotifications(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String projectUrl,
			@RequestBody @Valid NotificationSubscriptionReqDto request
	) {
		notificationManagementService.subscribe(userDetails.getUserId(), projectUrl, request);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "사용자 알림 구독 조회", description = "사용자가 구독한 알림 목록을 조회합니다.")
	@GetMapping("/projects/{projectUrl}/notifications/subscriptions")
	public ResponseEntity<SubscriptionsListResDto> getUserSubscriptions(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String projectUrl
	) {
		SubscriptionsListResDto subscriptions = notificationManagementService.getUserSubscriptions(userDetails.getUserId(), projectUrl);
		return ResponseEntity.ok(subscriptions);
	}

	@Operation(summary = "알림 구독 취소", description = "특정 알림 구독을 취소합니다.")
	@DeleteMapping("/projects/{projectUrl}/notifications/subscriptions/{subscriptionId}")
	public ResponseEntity<Void> unsubscribeFromNotifications(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long subscriptionId
	) {
		notificationManagementService.unsubscribe(userDetails.getUserId(), subscriptionId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "알림 예약", description = "특정 시간에 또는 지정된 시간 후에 알림을 예약합니다.")
	@PostMapping("/projects/{projectUrl}/notifications/schedule")
	public ResponseEntity<Void> scheduleNotification(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String projectUrl,
			@RequestBody @Valid ScheduleNotificationReqDto request
	) {
		scheduledNotificationService.scheduleNotification(userDetails.getUserId(), projectUrl, request);
		return ResponseEntity.ok().build();
	}

}
