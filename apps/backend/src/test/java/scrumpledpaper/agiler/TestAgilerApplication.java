package scrumpledpaper.agiler;

import org.springframework.boot.SpringApplication;

import scrumpledpaper.agiler.config.TestcontainersConfiguration;

public class TestAgilerApplication {

	public static void main(String[] args) {
		SpringApplication.from(AgilerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
