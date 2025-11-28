package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.image.entity.Image;

public class ImageFixture {

	public static Image createImage() {
		return Image.builder()
				.url("http://example.com/image.jpg")
				.objectKey("objectKey")
				.build();
	}

	public static Image createImage(String url) {
		return Image.builder()
				.url(url)
				.objectKey("objectKey")
				.build();
	}

	public static Image createImage(String url, String objectKey) {
		return Image.builder()
				.url(url)
				.objectKey(objectKey)
				.build();
	}

}
