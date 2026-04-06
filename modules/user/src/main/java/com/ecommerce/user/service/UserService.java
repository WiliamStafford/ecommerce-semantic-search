package com.ecommerce.user.service;

import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.request.ChangePasswordReq;
import com.ecommerce.user.dto.request.UserUpdateReq;

import java.util.List;

public interface UserService {

    User getProfile(String email);

    User updateProfile(String email, UserUpdateReq request);

    void changePassword(String email, ChangePasswordReq request);


    void validateUserActive(String email);

    User getProfileById(Long userId);

    void updateUserStatus(Long id, boolean b);


    List<User> findAllByRole(String role);

    Long findIdByEmail(String name);
}