package scrumpledpaper.agiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AgilerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgilerApplication.class, args);
	}

}
