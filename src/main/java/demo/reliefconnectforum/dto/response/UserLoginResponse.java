package demo.reliefconnectforum.dto.response;

import demo.reliefconnectforum.Enum.UserRoleEnum;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class UserLoginResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String userId;
    private String email;
    private UserRoleEnum role;
    private String refreshToken;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserLoginResponse response = new UserLoginResponse();

        public Builder accessToken(String accessToken) {
            response.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            response.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            response.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            response.expiresIn = expiresIn;
            return this;
        }

        public Builder userId(String userId) {
            response.userId = userId;
            return this;
        }

        public Builder email(String email) {
            response.email = email;
            return this;
        }

        public Builder role(UserRoleEnum role) {
            response.role = role;
            return this;
        }

        public UserLoginResponse build() {
            return response;
        }
    }
}
