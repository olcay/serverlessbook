package com.serverlessbook.lambda.imageresizer;

import com.google.inject.AbstractModule;
import com.serverlessbook.services.user.UserService;
import com.serverlessbook.services.user.UserServiceImpl;
import com.serverlessbook.services.user.repository.UserRepository;
import com.serverlessbook.services.user.repository.UserRepositoryDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.serverlessbook.repository.DynamoDBMapperWithCustomTableName;

public class DependencyInjectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserService.class).to(UserServiceImpl.class);
        bind(UserRepository.class).to(UserRepositoryDynamoDB.class);
        bind(DynamoDBMapper.class).to(DynamoDBMapperWithCustomTableName.class);
    }
}
