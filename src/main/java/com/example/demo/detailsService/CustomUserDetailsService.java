package com.example.demo.detailsService;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.models.User;
import com.example.demo.respositoris.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm kiếm người dùng từ cơ sở dữ liệu
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();
        String role = user.getRole(); // Giả sử roles là một chuỗi các role được phân cách bằng dấu phẩy
        grantedAuthorities.add(new SimpleGrantedAuthority(role.trim())); // Thêm các quyền vào danh sách

        // Tạo UserDetails với username, password và danh sách quyền
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) // Sử dụng email làm username
                .password(user.getPassword()) // Mật khẩu đã mã hóa từ database
                .authorities(grantedAuthorities) // Thêm danh sách quyền
                .build();
    }
}
