package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.UserAdminDTO;
import com.app.userservice.entity.user.Role;
import com.app.userservice.entity.user.User;
import com.app.userservice.repository.RoleRepository;
import com.app.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserAdminService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Lấy tất cả người dùng
     */
    public List<UserAdminDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy người dùng theo ID
     */
    public UserAdminDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        return convertToDTO(user);
    }
    
    /**
     * Tạo người dùng mới (bởi admin)
     */
    @Transactional
    public MessageResponse createUser(UserAdminDTO userDTO) {
        // Kiểm tra username tồn tại
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            return new MessageResponse("Username đã tồn tại", false);
        }
        
        // Kiểm tra email tồn tại
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return new MessageResponse("Email đã tồn tại", false);
        }
        
        // Tạo người dùng mới
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFullName(userDTO.getFullName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setProfileImage(userDTO.getProfileImage());
        user.setStatus(userDTO.getStatus());
        user.setEmailVerified(userDTO.isEmailVerified());
        user.setCreatedAt(LocalDateTime.now());
        
        // Gán vai trò
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại: " + roleName));
                roles.add(role);
            }
            
            user.setRoles(roles);
        } else {
            // Mặc định USER role
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Vai trò USER không tồn tại"));
            user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        }
        
        userRepository.save(user);
        
        return new MessageResponse("Người dùng đã được tạo thành công", true);
    }
    
    /**
     * Cập nhật người dùng
     */
    @Transactional
    public MessageResponse updateUser(Long id, UserAdminDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Kiểm tra username tồn tại
        if (!user.getUsername().equals(userDTO.getUsername()) && 
                userRepository.existsByUsername(userDTO.getUsername())) {
            return new MessageResponse("Username đã tồn tại", false);
        }
        
        // Kiểm tra email tồn tại
        if (!user.getEmail().equals(userDTO.getEmail()) && 
                userRepository.existsByEmail(userDTO.getEmail())) {
            return new MessageResponse("Email đã tồn tại", false);
        }
        
        // Cập nhật thông tin
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setPasswordChangedAt(LocalDateTime.now());
        }
        user.setFullName(userDTO.getFullName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setProfileImage(userDTO.getProfileImage());
        user.setStatus(userDTO.getStatus());
        user.setEmailVerified(userDTO.isEmailVerified());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        return new MessageResponse("Người dùng đã được cập nhật thành công", true);
    }
    
    /**
     * Xóa người dùng (soft delete bằng cách đặt status = 0)
     */
    @Transactional
    public MessageResponse deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Soft delete: đặt status = 0 (inactive)
        user.setStatus(0);
        userRepository.save(user);
        
        return new MessageResponse("Người dùng đã được vô hiệu hóa thành công", true);
    }
    
    /**
     * Cập nhật trạng thái người dùng
     */
    @Transactional
    public MessageResponse updateUserStatus(Long id, int status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        user.setStatus(status);
        userRepository.save(user);
        
        return new MessageResponse("Trạng thái người dùng đã được cập nhật thành công", true);
    }
    
    /**
     * Cập nhật vai trò người dùng
     */
    @Transactional
    public MessageResponse updateUserRoles(Long id, List<String> roleNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        Set<Role> roles = new HashSet<>();
        
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại: " + roleName));
            roles.add(role);
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        return new MessageResponse("Vai trò người dùng đã được cập nhật thành công", true);
    }
    
    /**
     * Khóa người dùng
     */
    @Transactional
    public MessageResponse lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());
        userRepository.save(user);
        
        return new MessageResponse("Tài khoản người dùng đã bị khóa", true);
    }
    
    /**
     * Mở khóa người dùng
     */
    @Transactional
    public MessageResponse unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        user.setFailedAttempt(0);
        userRepository.save(user);
        
        return new MessageResponse("Tài khoản người dùng đã được mở khóa", true);
    }
    
    /**
     * Lấy thống kê người dùng
     */
    public Map<String, Object> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long newUsersToday = userRepository.countNewUsersToday();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("newUsersToday", newUsersToday);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        
        return stats;
    }
    
    /**
     * Chuyển đổi từ User sang UserAdminDTO
     */
    private UserAdminDTO convertToDTO(User user) {
        UserAdminDTO dto = new UserAdminDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImage(user.getProfileImage());
        dto.setStatus(user.getStatus());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setTwoFactorEnabled(user.isTwoFactorEnabled());
        dto.setAccountNonLocked(user.isAccountNonLocked());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setLastActiveAt(user.getLastActiveAt());
        
        // Convert roles to role names
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        dto.setRoles(roleNames);
        
        return dto;
    }
}