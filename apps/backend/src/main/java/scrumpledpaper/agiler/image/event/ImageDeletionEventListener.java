package scrumpledpaper.agiler.image.event;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageDeletionEventListener {

	private final AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onImageDeleted(ImageDeletedEvent event) {
		try {
			amazonS3.deleteObject(bucket, event.objectKey());
		} catch (Exception e) {
			log.error("Failed to delete S3 object after DB commit: {}", event.objectKey(), e);
		}
	}

}
