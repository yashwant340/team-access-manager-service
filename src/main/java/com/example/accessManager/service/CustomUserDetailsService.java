package com.example.accessManager.service;

import com.example.accessManager.entity.User;
import com.example.accessManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println("Loaded user: " + user.getUsername());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // already hashed
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // or from DB
        );
    }
}

