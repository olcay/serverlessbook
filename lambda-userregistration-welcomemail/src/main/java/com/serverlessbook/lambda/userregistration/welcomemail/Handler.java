package com.serverlessbook.lambda.userregistration.welcomemail;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Objects;
import javax.inject.Inject;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class Handler implements RequestHandler<SNSEvent, Void> {

  private static final Injector INJECTOR = Guice.createInjector();

  LambdaLogger LOGGER;

  private AmazonSimpleEmailServiceClient simpleEmailServiceClient;

  @Inject
  public Handler setSimpleEmailServiceClient(
      AmazonSimpleEmailServiceClient simpleEmailServiceClient) {
    this.simpleEmailServiceClient = simpleEmailServiceClient;
    return this;
  }

  public Handler() {
    INJECTOR.injectMembers(this);
    Objects.nonNull(simpleEmailServiceClient);
  }

  private void sendEmail(final String emailAddress) {
    Destination destination = new Destination().withToAddresses(emailAddress);

    Message message = new Message()
        .withBody(new Body().withText(new Content("Welcome to our forum!")))
        .withSubject(new Content("Welcome!"));

    try {
      LOGGER.log("Sending welcome mail to " + emailAddress);
      simpleEmailServiceClient.sendEmail(new SendEmailRequest()
          .withDestination(destination)
          .withSource(System.getenv("SenderEmail"))
          .withMessage(message)
      );
      LOGGER.log("Sending welcome mail to " + emailAddress + " succeeded");
    } catch (Exception anyException) {
      LOGGER.log("Sending welcome mail to " + emailAddress + " failed: "+ anyException.getMessage());
    }

  }

  @Override
  public Void handleRequest(SNSEvent input, Context context) {
    LOGGER = context.getLogger();

    input.getRecords().forEach(snsMessage -> sendEmail(snsMessage.getSNS().getMessage()));
    return null;
  }
}
