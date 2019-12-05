package com.serverlessbook.services.user.repository;

import com.serverlessbook.services.user.domain.User;

import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface UserRepository {
    Optional<List<User>> getUsers();

    Optional<User> getUserByToken(String token);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByUsername(String username);

    void saveUser(User user);
}