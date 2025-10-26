package scrumpledpaper.agiler.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
	public AmazonS3 amazonS3Client(
			final LocalStackContainer localStackContainer,
			@Value("${cloud.aws.s3.bucket:agiler-test-bucket}") final String bucketName
	) {
		final AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(
				localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
				localStackContainer.getRegion()
		);

		final AWSCredentials credentials = new BasicAWSCredentials(
				localStackContainer.getAccessKey(),
				localStackContainer.getSecretKey()
		);

		final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(endpointConfig)
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withPathStyleAccessEnabled(true)
				.build();
		s3.createBucket(bucketName);
		return s3;
	}


}
