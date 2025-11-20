package com.example.fas.config.security;

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
    private final UserRepository userRepository;

    public CustomsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Hàm tải thông tin người dùng dựa trên tên đăng nhập.
     * Chi tiết hàm:
     * 1. Tìm người dùng trong cơ sở dữ liệu qua UserRepository.
     * 2. Nếu không tìm thấy, ném ra ngoại lệ UsernameNotFoundException.
     * 3. Lấy vai trò (role) của người dùng và chuyển đổi thành GrantedAuthority.
     * 4. Tạo và trả về đối tượng UserDetails chứa thông tin người dùng và quyền hạn.
     * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.fas.model.User user = userRepository.findByUsernameWithRole(username); // ← Sử dụng join fetch method

        if (user == null) throw new UsernameNotFoundException("User not found username : " + username);

        // Lấy role của user và gán cho GrantedAuthority < hệ thống phân quyền của Spring Security >
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName());

        // Lưu thông tin user và authorities vào UserDetails
        Collection<GrantedAuthority> authorities = List.of(grantedAuthority);
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
