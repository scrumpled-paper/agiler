package scrumpledpaper.agiler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import scrumpledpaper.agiler.config.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AgilerApplicationTests {

	@Test
	void contextLoads() {
	}

}
