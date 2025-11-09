package demo.reliefconnectforum.service.auth;

import demo.reliefconnectforum.dto.request.UserLoginRequest;
import demo.reliefconnectforum.dto.request.UserRegisterRequest;
import demo.reliefconnectforum.dto.response.UserLoginResponse;
import demo.reliefconnectforum.dto.response.UserRegisterResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    UserRegisterResponse register(UserRegisterRequest request);
    UserLoginResponse login(UserLoginRequest request);
    UserLoginResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}
