package com.app.userservice.service;

import com.app.userservice.dto.MessageResponse;
import com.app.userservice.dto.RoleDTO;
import com.app.userservice.entity.user.Role;
import com.app.userservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleAdminService {

    @Autowired
    private RoleRepository roleRepository;
    
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
        return convertToDTO(role);
    }
    
    @Transactional
    public MessageResponse createRole(RoleDTO roleDTO) {
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            return new MessageResponse("Tên vai trò đã tồn tại", false);
        }
        
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setSystem(false);
        
        roleRepository.save(role);
        
        return new MessageResponse("Vai trò đã được tạo thành công", true);
    }
    
    @Transactional
    public MessageResponse updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
        
        if (role.isSystem()) {
            return new MessageResponse("Không thể cập nhật vai trò hệ thống", false);
        }
        
        if (!role.getName().equals(roleDTO.getName()) && 
                roleRepository.findByName(roleDTO.getName()).isPresent()) {
            return new MessageResponse("Tên vai trò đã tồn tại", false);
        }
        
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        
        roleRepository.save(role);
        
        return new MessageResponse("Vai trò đã được cập nhật thành công", true);
    }
    
    @Transactional
    public MessageResponse deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
        
        if (role.isSystem()) {
            return new MessageResponse("Không thể xóa vai trò hệ thống", false);
        }
        
        roleRepository.delete(role);
        
        return new MessageResponse("Vai trò đã được xóa thành công", true);
    }
    
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystem(role.isSystem());
        
        return dto;
    }
}