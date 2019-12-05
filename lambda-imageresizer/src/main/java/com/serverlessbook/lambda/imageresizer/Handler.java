package com.serverlessbook.lambda.imageresizer;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.inject.Inject;

public class Handler implements RequestHandler<S3Event, Void> {

  private static final Injector INJECTOR = Guice.createInjector(new DependencyInjectionModule());

  LambdaLogger logger;

  final AmazonS3 s3client;

  private AmazonSNSClient amazonSNSClient;

  @Inject
  public Handler setAmazonSNSClient(AmazonSNSClient amazonSNSClient) {
      this.amazonSNSClient = amazonSNSClient;
      return this;
  }

  private void notifySnsSubscribers() {
    try {
       logger.log(System.getenv("FileUploadSnsTopic"));
      amazonSNSClient.publish(System.getenv("FileUploadSnsTopic"), "new file uploaded");
      logger.log("SNS notification sent for a new file");
    } catch (Exception anyException) {
      logger.log("SNS notification failed for a new file; "+ anyException.getMessage());
    }
  }

  public Handler() {
    s3client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
    INJECTOR.injectMembers(this);
  }

  private void resizeImage(String bucket, String key) {
    logger.log("Resizing s3://" + bucket + "/" + key);
    
    final String userId = s3client.getObjectMetadata(bucket, key).getUserMetaDataOf("user-id");
    logger.log("Image is belonging to " + userId);
    final String uploaderUserId = s3client.getObjectMetadata(bucket, key).getUserMetaDataOf("uploader-user-id");
    logger.log("Image is uploaded by " + uploaderUserId);
    final String destinationKey = "users/" + userId + "/picture/small.jpg";
    logger.log("Image is starting to be copied to s3://" + bucket + "/" + destinationKey);
    s3client.copyObject(bucket, key, bucket, destinationKey);
    logger.log("Image has been copied to s3://" + bucket + "/" + destinationKey);
    
  }

  @Override
  public Void handleRequest(S3Event input, Context context) {
    logger = context.getLogger();

    input.getRecords().forEach(s3EventNotificationRecord ->
        resizeImage(s3EventNotificationRecord.getS3().getBucket().getName(),
            s3EventNotificationRecord.getS3().getObject().getKey()));
    notifySnsSubscribers();
    return null;
  }
}
