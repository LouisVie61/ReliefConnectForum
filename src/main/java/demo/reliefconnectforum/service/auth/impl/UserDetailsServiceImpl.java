package demo.reliefconnectforum.service.auth.impl;

import demo.reliefconnectforum.entity.User;
import demo.reliefconnectforum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private static String normalizeRoleToAuthority(String roleFromDb) {
        if (roleFromDb == null || roleFromDb.isBlank()) return "USER";
        return roleFromDb;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User appUser = userRepository.findByEmail(email);

        if (appUser == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        String authority = normalizeRoleToAuthority(appUser.getRole().name());
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

        return org.springframework.security.core.userdetails.User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities(authorities)
                .accountExpired(false)
//                .accountLocked(!appUser.isActive())
                .credentialsExpired(false)
//                .disabled(!appUser.isActive())
                .build();
    }
}