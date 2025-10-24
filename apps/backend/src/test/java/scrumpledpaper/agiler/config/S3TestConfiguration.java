package scrumpledpaper.agiler.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class S3TestConfiguration {

	@Bean
	public LocalStackContainer localStackContainer() {
		LocalStackContainer c = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"))
				.withServices(org.testcontainers.containers.localstack.LocalStackContainer.Service.S3);
		c.start();
		return c;
	}

	@Bean
	@Primary
	public AmazonS3 amazonS3Client(LocalStackContainer localStackContainer) {
		AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(
				localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
				localStackContainer.getRegion()
		);

		AWSCredentials credentials = new BasicAWSCredentials(
				localStackContainer.getAccessKey(),
				localStackContainer.getSecretKey()
		);

		return AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(endpointConfig)
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withPathStyleAccessEnabled(true)
				.build();
	}

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry r) {
		r.add("cloud.aws.s3.bucket", () -> "agiler-test-bucket");
	}

}
