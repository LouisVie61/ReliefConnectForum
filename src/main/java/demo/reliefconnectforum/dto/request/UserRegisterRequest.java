package demo.reliefconnectforum.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRegisterRequest {
    private String username;
    private String fullname;
    private String email;
    private String password;
    private String phone;
    private String address;
}
