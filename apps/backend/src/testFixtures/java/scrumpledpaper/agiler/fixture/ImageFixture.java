package scrumpledpaper.agiler.fixture;

import org.springframework.stereotype.Component;

import scrumpledpaper.agiler.image.entity.Image;

public class ImageFixture {

	public static Image createImage() {
		return Image.builder()
			.url("http://example.com/image.jpg")
			.build();
	}
}
