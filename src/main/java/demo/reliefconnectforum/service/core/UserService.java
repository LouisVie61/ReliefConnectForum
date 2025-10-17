package demo.reliefconnectforum.service.core;

import demo.reliefconnectforum.dto.request.UserRequest;
import demo.reliefconnectforum.dto.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface UserService {
    UserResponse activateAccount(UUID userId, String otp);
    UserResponse getUserById(UUID id);
    UserResponse create(UserRequest request);
    UserResponse update(UUID id, UserRequest request);
}
