package com.app.userservice.security.service;

import com.app.userservice.entity.user.User;
import com.app.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // Check if user account is locked
        if (!user.isAccountNonLocked()) {
            throw new UsernameNotFoundException("User account is locked");
        }
        
        // Check if user is active (status == 1)
        if (user.getStatus() != 1) {
            throw new UsernameNotFoundException("User account is not active");
        }

        return UserDetailsImpl.build(user);
    }
}