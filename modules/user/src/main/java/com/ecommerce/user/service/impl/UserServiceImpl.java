package com.ecommerce.user.service.impl;

import com.ecommerce.user.domain.Address;
import com.ecommerce.user.domain.Role;
import com.ecommerce.user.domain.SellerRegistrations;
import com.ecommerce.user.domain.User;
import com.ecommerce.user.dto.request.ChangePasswordReq;
import com.ecommerce.user.dto.request.SellerRegistrationReq;
import com.ecommerce.user.dto.request.UserUpdateReq;
import com.ecommerce.user.dto.response.SellerRegistrationDTO;
import com.ecommerce.user.dto.response.UserResponseDTO;
import com.ecommerce.user.enums.RegistrationStatus;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.SellerRegistrationsRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, com.ecommerce.product.service.UserLookupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SellerRegistrationsRepository sellerRegistrationRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;

    @Override
    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
    }

    @Override
    @Transactional
    public User updateProfile(String email, UserUpdateReq request) {
        User user = getProfile(email);

        if (request.fullName() != null && !request.fullName().isBlank()) user.setFullName(request.fullName());
        if (request.avatar() != null && !request.avatar().isBlank()) user.setAvatar(request.avatar());
        if (request.age() != null) user.setAge(request.age());
        if (request.phone() != null && !request.phone().isBlank()) user.setPhone(request.phone());

        Address address = user.getAddresses().stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(user.getAddresses().stream().findFirst().orElse(null));

        if (address == null) {
            address = Address.builder().user(user).isDefault(true).build();
            user.addAddress(address);
        }

        if (request.province() != null && !request.province().isBlank()) address.setProvince(request.province());
        if (request.district() != null && !request.district().isBlank()) address.setDistrict(request.district());
        if (request.ward() != null && !request.ward().isBlank()) address.setWard(request.ward());
        if (request.street() != null && !request.street().isBlank()) address.setStreet(request.street());
        if (request.houseNumber() != null && !request.houseNumber().isBlank())
            address.setHouseNumber(request.houseNumber());

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
        if (!user.isEnabled()) {
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

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException(" KHông tìm thấy người dùng với id:" + userId));
    }

    @Override
    @Transactional
    public void promoteToSeller(Long registrationId) {
        SellerRegistrations reg = sellerRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký!"));

        User user = reg.getUser();
        Role sellerRole = roleRepository.findByRoleName("ROLE_SELLER");
        if (sellerRole != null && !user.getRoles().contains(sellerRole)) {
            user.getRoles().add(sellerRole);
            userRepository.save(user);
        }
        reg.setStatus(RegistrationStatus.ACTIVE);
        reg.setUpdatedAt(LocalDateTime.now());
        sellerRegistrationRepository.save(reg);
    }

    @Override
    public String getSellerRegistrationStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        return sellerRegistrationRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(reg -> reg.getStatus().toString())
                .orElse("NOT_REGISTERED");
    }

    @Override
    @Transactional
    public void submitRegistration(String email, SellerRegistrationReq request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        boolean exists = sellerRegistrationRepository.existsByUser_IdAndStatus(user.getId(), RegistrationStatus.PENDING);
        if (exists) {
            throw new RuntimeException("Bạn đã có một đơn đăng ký đang chờ phê duyệt!");
        }
        SellerRegistrations reg = new SellerRegistrations();
        reg.setUser(user);
        reg.setShopName(request.shopName());
        reg.setAddress(request.address());
        reg.setDescription(request.description());
        reg.setStatus(RegistrationStatus.PENDING);
        reg.setCreatedAt(LocalDateTime.now()); //
        sellerRegistrationRepository.save(reg);
    }

    @Override
    public List<SellerRegistrationDTO> findAllPending() {
        return sellerRegistrationRepository.findByStatus(RegistrationStatus.valueOf("PENDING"))
                .stream()
                .map(SellerRegistrationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void rejectRegistration(Long id) {
        SellerRegistrations reg = sellerRegistrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đăng ký với ID: " + id));
        reg.setStatus(RegistrationStatus.REJECTED);
        reg.setUpdatedAt(LocalDateTime.now());
        sellerRegistrationRepository.save(reg);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void closeShop(Long id) {

        SellerRegistrations shop = sellerRegistrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Shop với ID: " + id));
        shop.setStatus(RegistrationStatus.CLOSED);
        sellerRegistrationRepository.save(shop);
    }

    //    @Override
//    public List<SellerRegistrations> findAllShops() {
//        return sellerRegistrationRepository.findByStatus(RegistrationStatus.valueOf("ACTIVE"));
//    }
    @Override
    public List<SellerRegistrationDTO> findAllShops() {
        return sellerRegistrationRepository.findByStatus(RegistrationStatus.ACTIVE)
                .stream()
                .map(SellerRegistrationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    @Override
    @Transactional
    public void updateUserByAdmin(Long id, UserUpdateReq request) {
        // 1. Tìm user kèm địa chỉ
        User user = userRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));

        // 2. Cập nhật thông tin User (Check thay đổi để tránh cập nhật thừa)
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }

        if (request.age() != null) {
            user.setAge(request.age());
        }

        // 3. Xử lý Phone: Chỉ check trùng nếu phone mới khác phone cũ
        if (request.phone() != null && !request.phone().isBlank()) {
            if (!request.phone().equals(user.getPhone())) {
                // Kiểm tra xem số mới có đang thuộc về user khác không
                if (userRepository.existsByPhone(request.phone())) {
                    throw new RuntimeException("Số điện thoại " + request.phone() + " đã được sử dụng bởi người dùng khác!");
                }
                user.setPhone(request.phone());
            }
        }

        // 4. Xử lý Address an toàn
        Address address = user.getAddresses().stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElseGet(() -> user.getAddresses().isEmpty() ? null : user.getAddresses().iterator().next());

        if (address == null) {
            address = Address.builder()
                    .user(user)
                    .isDefault(true)
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .build();
            user.addAddress(address);
        }

        // 5. Cập nhật địa chỉ với dữ liệu sạch
        if (request.province() != null) address.setProvince(request.province());
        if (request.district() != null) address.setDistrict(request.district());
        if (request.ward() != null) address.setWard(request.ward());
        if (request.street() != null) address.setStreet(request.street());
        if (request.houseNumber() != null) address.setHouseNumber(request.houseNumber());

        // Vì có @Transactional, user đã được quản lý (managed),
        // Hibernate sẽ tự động lưu thay đổi khi kết thúc hàm mà không cần gọi save() tường minh.
        // Tuy nhiên, giữ lại nếu bạn muốn flush dữ liệu ngay lập tức.
        userRepository.save(user);
    }


    @Override
    public String getSellerName(Long sellerId) {
        return userRepository.findById(sellerId).map(user -> user.getFullName()).orElse("Người bán");
    }

    @Override
    public String getSellerEmail(Long sellerId) {
        return userRepository.findById(sellerId).map(user -> user.getEmail()).orElse("Chưa có email");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersWithAddress() {
        return userRepository.findAllWithAddresses().stream().map(user -> {
            Address addr = user.getAddresses().stream()
                    .filter(Address::isDefault).findFirst()
                    .orElse(user.getAddresses().stream().findFirst().orElse(null));

            return new UserResponseDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhone(),
                    user.getAge(),
                    user.isEnabled(),
                    addr != null ? addr.getProvince() : "",
                    addr != null ? addr.getDistrict() : "",
                    addr != null ? addr.getWard() : "",
                    addr != null ? addr.getStreet() : "",
                    addr != null ? addr.getHouseNumber() : ""
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAllUsersWithAddressByRole(String role) {
        return userRepository.findAllByRoleName(role.toUpperCase()).stream().map(user -> {
            Address addr = user.getAddresses().stream()
                    .filter(Address::isDefault).findFirst()
                    .orElse(user.getAddresses().stream().findFirst().orElse(null));

            return new UserResponseDTO(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAge(),
                    user.isEnabled(),
                    addr != null ? addr.getProvince() : "",
                    addr != null ? addr.getDistrict() : "",
                    addr != null ? addr.getWard() : "",
                    addr != null ? addr.getStreet() : "",
                    addr != null ? addr.getHouseNumber() : ""
            );
        }).collect(Collectors.toList());
    }

}