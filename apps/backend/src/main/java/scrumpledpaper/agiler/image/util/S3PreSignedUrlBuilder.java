package scrumpledpaper.agiler.image.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import scrumpledpaper.agiler.image.enums.ImageContentType;

import java.util.Date;
import java.util.UUID;

public class S3PreSignedUrlBuilder {

	public static GeneratePresignedUrlRequest build(String objectKey, String contentType, String bucket) {
		Date expiration = getExpirationTime();
		ImageContentType imageContentType = ImageContentType.from(contentType);

		return new GeneratePresignedUrlRequest(bucket, objectKey)
				.withMethod(HttpMethod.PUT)
				.withExpiration(expiration)
				.withContentType(imageContentType.getMimeType());
	}

	public static String generateObjectKey(long userId, String fileName) {
		String uuid = UUID.randomUUID().toString();

		return String.format("images/%d/%s-%s", userId, uuid, fileName);
	}

	private static Date getExpirationTime() {
		long expTimeMillis = System.currentTimeMillis();
		expTimeMillis += 1000 * 60 * 5; // 5 minutes

		return new Date(expTimeMillis);
	}

}
