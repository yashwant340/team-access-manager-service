package com.example.accessManager.service.impl;

import com.example.accessManager.config.SecurityConfig;
import com.example.accessManager.dto.AccessControlDTO;
import com.example.accessManager.dto.AccessRequestDTO;
import com.example.accessManager.dto.LoginRequestDTO;
import com.example.accessManager.entity.AccessRequest;
import com.example.accessManager.entity.LoginRequest;
import com.example.accessManager.entity.Team;
import com.example.accessManager.entity.User;
import com.example.accessManager.enums.AccessMode;
import com.example.accessManager.enums.PendingRequestStatus;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.repository.LoginRequestRepository;
import com.example.accessManager.repository.UserRepository;
import com.example.accessManager.service.AdminService;
import com.example.accessManager.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final LoginRequestRepository loginRequestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    private final SecurityConfig securityConfig;

    @Override
    public List<LoginRequestDTO> getAllLoginRequests() {
        List<LoginRequest> loginRequests = loginRequestRepository.findAllByIsActiveTrue();
        List<LoginRequestDTO> loginRequestDTOS = new ArrayList<>();
        loginRequests.forEach(x -> {
            LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
                    .id(x.getId())
                    .name(x.getName())
                    .email(x.getEmail())
                    .empId(x.getEmpId())
                    .team(x.getTeam())
                    .role(x.getRole())
                    .createdDate(dateFormat.format(x.getCreatedDate()))
                    .build();
            loginRequestDTOS.add(loginRequestDTO);
        });
        return loginRequestDTOS;
    }

    @Override
    public void approveRequest(Long id, Long teamId) {
        LoginRequest loginRequest = loginRequestRepository.findByIdAndIsActiveTrue(id);
        loginRequest.setIsActive(false);
        loginRequest.setUpdatedDate(new Date());

        String tempPassword = generateTempPassword();
        String hashedPassword = securityConfig.passwordEncoder().encode(tempPassword);

        User user = new User();
        user.setName(loginRequest.getName());
        user.setEmail(loginRequest.getEmail());
        user.setUsername(loginRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setEmpId(Long.valueOf(loginRequest.getEmpId()));
        user.setIsActive(true);
        user.setTeam(Team.builder().id(teamId).build());
        user.setRole(loginRequest.getRole());
        user.setAccessMode(AccessMode.INHERIT_TEAM_ACCESS);
        user.setCreatedDate(new Date());
        user.setPlatformRole("USER");
        userRepository.save(user);

        loginRequestRepository.save(loginRequest);

        emailService.sendApprovalEmail(
                loginRequest.getEmail(),
                loginRequest.getEmail(),
                tempPassword,
                "https://app.company.com/login"
        );

    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
