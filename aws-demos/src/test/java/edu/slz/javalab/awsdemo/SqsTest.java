package edu.slz.javalab.awsdemo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;

@SpringBootTest
public class SqsTest {
  private static final Logger logger = LoggerFactory.getLogger(SqsTest.class);

  @Test
  public void testSqs() {
    SqsClient sqsClient = SqsClient.builder().region(Region.US_EAST_1)
      .credentialsProvider(ProfileCredentialsProvider.create()).build();

    ListQueuesResponse queues = sqsClient.listQueues();

    logger.info("SQS queues: {}", queues);
  }

}
