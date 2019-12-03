package com.serverlessbook.lambda.imageresizer;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class Handler implements RequestHandler<S3Event, Void> {

  LambdaLogger logger;

  final AmazonS3 s3client;

  public Handler() {
    s3client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
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
    return null;
  }
}
