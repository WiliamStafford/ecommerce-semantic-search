package com.ecommerce.user.service;

import com.ecommerce.user.domain.SellerRegistrations;
import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.request.ChangePasswordReq;
import com.ecommerce.user.dto.request.SellerRegistrationReq;
import com.ecommerce.user.dto.request.UserUpdateReq;
import com.ecommerce.user.dto.response.SellerRegistrationDTO;
import com.ecommerce.user.dto.response.UserResponseDTO;

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

    User getUserById(Long userId);


//seller treatment
    void promoteToSeller(Long id);

    String getSellerRegistrationStatus(String email);

    void submitRegistration(String email, SellerRegistrationReq request);

    //admin treatment
    List<SellerRegistrationDTO> findAllPending();

    void rejectRegistration(Long id);
    List<User> findAllUsers();
    void blockUser(Long id);
    void closeShop(Long id);



    List<SellerRegistrationDTO> findAllShops();

    User findById(Long userId);

    void updateUserByAdmin(Long id, UserUpdateReq request);

    List<UserResponseDTO> getAllUsersWithAddress();
    List<UserResponseDTO> findAllUsersWithAddressByRole(String role);
}