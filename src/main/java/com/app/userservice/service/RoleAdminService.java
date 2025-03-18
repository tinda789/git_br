package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.RoleDTO;
import com.app.userservice.entity.user.Permission;
import com.app.userservice.entity.user.Role;
import com.app.userservice.repository.PermissionRepository;
import com.app.userservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleAdminService {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    /**
     * Lấy tất cả vai trò
     */
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy vai trò theo ID
     */
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
        return convertToDTO(role);
    }
    
    /**
     * Tạo vai trò mới
     */
    @Transactional
    public MessageResponse createRole(RoleDTO roleDTO) {
        // Kiểm tra tên vai trò đã tồn tại
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            return new MessageResponse("Tên vai trò đã tồn tại", false);
        }
        
        // Tạo vai trò mới
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setSystem(false); // Vai trò tùy chỉnh không phải vai trò hệ thống
        
        // Gán quyền
        if (roleDTO.getPermissions() != null && !roleDTO.getPermissions().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            
            for (String permissionName : roleDTO.getPermissions()) {
                Permission permission = permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new RuntimeException("Quyền không tồn tại: " + permissionName));
                permissions.add(permission);
            }
            
            role.setPermissions(permissions);
        }
        
        roleRepository.save(role);
        
        return new MessageResponse("Vai trò đã được tạo thành công", true);
    }
    
    /**
     * Cập nhật vai trò
     */
    @Transactional
    public MessageResponse updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
        
        // Không cho phép cập nhật vai trò hệ thống
        if (role.isSystem()) {
            return new MessageResponse("Không thể cập nhật vai trò hệ thống", false);
        }
        
        // Kiểm tra tên vai trò đã tồn tại
        if (!role.getName().equals(roleDTO.getName()) && 
                roleRepository.findByName(roleDTO.getName()).isPresent()) {
            return new MessageResponse("Tên vai trò đã tồn tại", false);
        }
        
        // Cập nhật thông tin
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        
        roleRepository.save(role);
        
        return new MessageResponse("Vai trò đã được cập nhật thành công", true);
    }
    
    /**
     * Xóa vai trò
     */
    @Transactional
    public MessageResponse deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
        
        // Không cho phép xóa vai trò hệ thống
        if (role.isSystem()) {
            return new MessageResponse("Không thể xóa vai trò hệ thống", false);
        }
        
        roleRepository.delete(role);
        
        return new MessageResponse("Vai trò đã được xóa thành công", true);
    }
    
    /**
     * Cập nhật quyền cho vai trò
     */
    @Transactional
    public MessageResponse updateRolePermissions(Long id, List<String> permissionNames) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
        
        // Không cho phép cập nhật quyền cho vai trò hệ thống
        if (role.isSystem()) {
            return new MessageResponse("Không thể cập nhật quyền cho vai trò hệ thống", false);
        }
        
        Set<Permission> permissions = new HashSet<>();
        
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException("Quyền không tồn tại: " + permissionName));
            permissions.add(permission);
        }
        
        role.setPermissions(permissions);
        roleRepository.save(role);
        
        return new MessageResponse("Quyền cho vai trò đã được cập nhật thành công", true);
    }
    
    /**
     * Chuyển đổi từ Role sang RoleDTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystem(role.isSystem());
        
        // Convert permissions to permission names
        List<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
        dto.setPermissions(permissionNames);
        
        return dto;
    }
}