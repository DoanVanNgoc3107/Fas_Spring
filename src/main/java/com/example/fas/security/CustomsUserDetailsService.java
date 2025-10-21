package com.example.fas.security;

import com.example.fas.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CustomsUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    public CustomsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.fas.model.User user = userRepository.findByUsername(username);

        if (user == null) throw new UsernameNotFoundException("User not found username : " + username);

        // Lấy role của user và gán cho GrantedAuthority < hệ thống phân quyền của Spring Security >
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // Lưu thông tin user và authorities vào UserDetails
        Collection<GrantedAuthority> authorities = List.of(grantedAuthority);
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
