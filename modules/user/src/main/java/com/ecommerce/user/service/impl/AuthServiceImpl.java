package com.ecommerce.user.service.impl;

import com.ecommerce.common.security.JwtProvider;
import com.ecommerce.user.domain.RefreshToken;
import com.ecommerce.user.domain.Role;
import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.request.AuthRequest;
import com.ecommerce.user.dto.request.RegisterRequest;
import com.ecommerce.user.dto.response.AuthResponse;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.AuthService;
import com.ecommerce.user.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
     private final UserRepository userRepository;
     private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        Role defaultRole = roleRepository.findByRoleName("ROLE_CUSTOMER");
        if(defaultRole == null)throw new RuntimeException("Không tìm thấy role");
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .enabled(true)
                .roles(Set.of( defaultRole))
                .build();
        List<String> rolesForToken = user.getRoles().stream()
                .map(r -> String.valueOf(r.getName()))
                .toList();
        userRepository.save(user);
        log.info("Đã đăng ký thành công user mới: {}", user.getEmail());

        String accessToken = jwtProvider.generateToken(user.getEmail(),rolesForToken);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                "Đăng ký thành công!"
        );
    }
    @Override
    public AuthResponse authenticate(AuthRequest request) {
        log.info("phương thức xấc thực : {}", request.email());
        return switch (request.authType()) {
            case PASSWORD -> handlePasswordLogin(request);
            case OTP -> handleOTPLogin(request);
            case MAGIC_LINK -> handleMagicLinkLogin(request);
        };
    }

    private AuthResponse handleMagicLinkLogin(AuthRequest request) {
        return null;
    }

    private AuthResponse handlePasswordLogin(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        if (request.password() == null || request.password().isBlank()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        String accessToken = jwtProvider.generateToken(user.getEmail(),roles);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        log.info("Đăng nhập thành công cho: {}", user.getEmail());
        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                "Đăng nhập thành công"
        );
    }

    private AuthResponse handleOTPLogin(AuthRequest request) {
        log.info("đã gửi mã OTP tới email: {}", request.email());
        return new AuthResponse(null, null, request.email(), "Mã OTP đã được gửi tới Email của bạn");
    }


}