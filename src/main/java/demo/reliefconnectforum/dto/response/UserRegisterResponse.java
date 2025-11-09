package demo.reliefconnectforum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private LocalDateTime createdAt;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserRegisterResponse response = new UserRegisterResponse();

        public Builder id(UUID userId) {
            response.id = userId;
            return this;
        }

        public Builder username(String username) {
            response.username = username;
            return this;
        }

        public Builder email(String email) {
            response.email = email;
            return this;
        }

        public Builder fullName(String fullName) {
            response.fullName = fullName;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            response.phoneNumber = phoneNumber;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public UserRegisterResponse build() {
            return response;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
