package com.ecommerce.user.service.impl;

import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.request.ChangePasswordReq;
import com.ecommerce.user.dto.request.UserUpdateReq;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
    }

    @Override
    @Transactional
    public User updateProfile(String email, UserUpdateReq request) {
        User user = getProfile(email);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }

        if (request.avatar() != null && !request.avatar().isBlank()) {
            user.setAvatar(request.avatar());
        }

        if (request.age() != null) {
            user.setAge(request.age());
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordReq request) {
        User user = getProfile(email);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void validateUserActive(String email) {
        User user = getProfile(email);
        if (!user.getEnabled()) {
            throw new RuntimeException("Tài khoản của bạn đã bị Admin vô hiệu hóa!");
        }
    }

    @Override
    public User getProfileById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, boolean status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User ID: " + id));

        user.setEnabled(status);
        userRepository.save(user);
    }

    @Override
    public List<User> findAllByRole(String role) {
        return userRepository.findAllByRoleName(role.toUpperCase());
    }

    @Override
    public Long findIdByEmail(String name) {
        User user = userRepository.findByEmail(name)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + name));

        return user.getId();
    }


}