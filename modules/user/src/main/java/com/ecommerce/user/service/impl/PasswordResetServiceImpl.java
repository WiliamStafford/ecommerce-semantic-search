package com.ecommerce.user.service.impl;

import com.ecommerce.user.domain.PasswordResetCode;
import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.request.ResetPasswordRequest;
import com.ecommerce.user.repository.PasswordResetRepository;
import com.ecommerce.user.service.PasswordResetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final JavaMailSender mailSender;
    private final PasswordResetRepository resetRepository;
    private final com.ecommerce.user.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public void sendResetCode(String email) {
         resetRepository.deleteByEmail(email);
        String code = String.format("%06d", new Random().nextInt(1000000));

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .email(email)
                .code(code)
                .expirationTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        resetRepository.save(resetCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("levuhung678@gmail.com");
        message.setTo(email);
        message.setSubject("Mã xác nhận khôi phục mật khẩu - Ecommerce App");

        String emailContent = "\n\n" +
                              "Mã xác nhận của bạn là: " + code + "\n\n" +
                              "Mã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này để bảo mật tài khoản.";

        message.setText(emailContent);
        mailSender.send(message);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetCode resetCode = resetRepository.findByEmailAndCode(request.email(), request.code())
                .orElseThrow(() -> new RuntimeException("Mã xác nhận hoặc Email không chính xác!"));

        if (resetCode.isExpired()) {
            throw new RuntimeException("Mã này đã hết hạn sử dụng!");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email này!"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetRepository.delete(resetCode);
    }
}