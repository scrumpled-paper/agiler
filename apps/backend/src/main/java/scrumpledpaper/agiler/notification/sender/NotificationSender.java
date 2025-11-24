package scrumpledpaper.agiler.notification.sender;

public interface NotificationSender {
    boolean supports(String channelType);
    void send(String webhookUrl, String message);
}
