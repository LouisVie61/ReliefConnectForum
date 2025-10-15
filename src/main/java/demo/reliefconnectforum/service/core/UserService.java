package demo.reliefconnectforum.service;

import demo.reliefconnectforum.dto.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface UserService {
    UserResponse activateAccount(UUID userId, String otp);
    UserResponse getUserById(UUID id);
}
