package demo.reliefconnectforum.dto.request;

import demo.reliefconnectforum.Enum.UserRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private UserRoleEnum role;
    private String phoneNumber;
    private String address;
}
