package com.example.cloud.service;

import com.example.cloud.domain.User;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/3/10 23:42
 */
public interface UserService {
    void create(User user);

    User getUser(Long id);

    void update(User user);

    void delete(Long id);

    User getByUsername(String username);

    List<User> getUserByIds(List<Long> ids);
}
