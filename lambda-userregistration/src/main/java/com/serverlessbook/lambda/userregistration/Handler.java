package com.serverlessbook.lambda.userregistration;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.serverlessbook.lambda.LambdaHandler;
import com.serverlessbook.services.user.UserService;
import com.serverlessbook.services.user.domain.User;

import javax.inject.Inject;
import java.util.Objects;

public class Handler extends LambdaHandler<Handler.RegistrationInput, Handler.RegistrationOutput> {

    public static class RegistrationInput {

        @JsonProperty("username")
        private String username;

        @JsonProperty("email")
        private String email;

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class RegistrationOutput {

        private final String resourceUrl;

        public RegistrationOutput(User user) {
            resourceUrl = "/user/" + user.getId();
        }

        @JsonGetter("resourceUrl")
        public String getResourceUrl() {
            return resourceUrl;
        }
    }

    private static final Injector INJECTOR = Guice.createInjector(new DependencyInjectionModule());

    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    LambdaLogger logger;

    private AmazonSNSClient amazonSNSClient;

    @Inject
    public Handler setAmazonSNSClient(AmazonSNSClient amazonSNSClient) {
        this.amazonSNSClient = amazonSNSClient;
        return this;
    }

    private void notifySnsSubscribers(User user) {
      try {
        amazonSNSClient.publish(System.getenv("UserRegistrationSnsTopic"), user.getEmail());
        logger.log("SNS notification sent for "+user.getEmail());
      } catch (Exception anyException) {
        logger.log("SNS notification failed for "+user.getEmail()+" as "+ anyException.getMessage());
      }
    }

    public Handler() {
        INJECTOR.injectMembers(this);
        Objects.requireNonNull(userService);
    }

    @Override
    public RegistrationOutput handleRequest(RegistrationInput input, Context context) {
        logger = context.getLogger();
        
        User createdUser = userService.registerNewUser(input.username, input.email);
        notifySnsSubscribers(createdUser);
        return new RegistrationOutput(createdUser);
    }
}
