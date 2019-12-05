package com.serverlessbook.services.user;

import com.serverlessbook.services.user.domain.User;
import com.serverlessbook.services.user.exception.UserRegistrationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface UserService {
    List<User> getUsers() throws UserNotFoundException;

    User getUserByToken(String token) throws UserNotFoundException;

    User registerNewUser(String username, String email) throws UserRegistrationException;
}