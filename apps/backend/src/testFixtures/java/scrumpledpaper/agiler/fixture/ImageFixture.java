package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.image.entity.Image;

public class ImageFixture {

	public static Image createImage() {
		return Image.builder()
				.url("http://example.com/image.jpg")
				.objectKey("objectKey")
				.build();
	}
}
