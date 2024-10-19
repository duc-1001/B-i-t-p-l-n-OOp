package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.models.User;
import com.example.demo.respositoris.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/profile")
public class UserController {

    @Autowired
    private UserRepository userRepository;
     @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("")
    public String getProfileUser(ModelMap modelMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByEmail(username);
            modelMap.addAttribute("user", user);
            return "profileUser";
        }
        else{
            return "redirect:/login";
        }
    }

    @PostMapping("/update")
    public String updateUser(@RequestParam("username") String username,
            @RequestParam("id") String id,
            @RequestParam Map<String, String> params,
            @RequestParam(value = "file", required = false) MultipartFile file,
            ModelMap modelMap) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(username);

            if (file != null && !file.isEmpty()) {
                try {
                    String imageUrl = saveFile(file);
                    user.setAvatarUrl(imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            userRepository.save(user);
            modelMap.addAttribute("message", "Cập nhật thành công!");
            return "redirect:/profile";
        } else {
            modelMap.addAttribute("errorMessage", "Người dùng không tồn tại!");
            return "error";
        }
    }

    @GetMapping("change-password")
    public String changePassword() {
        return "changePassword";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmNewPassword") String confirmNewPassword,
            ModelMap modelMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            User user = userRepository.findByEmail(currentUsername);

            

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                modelMap.addAttribute("error", "Mật khẩu hiện tại không đúng");
                return "changePassword";
            }

            if(newPassword.length()<6){
                modelMap.addAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
                return "changePassword";
            }

            if (!newPassword.equals(confirmNewPassword)) {
                modelMap.addAttribute("error", "Mật khẩu mới và xác nhận không khớp");
                return "changePassword";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            modelMap.addAttribute("message", "Đổi mật khẩu thành công!");
            return "redirect:/profile";
        } else {
            modelMap.addAttribute("error", "Người dùng không tồn tại");
            return "error";
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        String uploadDir = "D:/demo/src/main/resources/static/images/";
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), filePath);
        return "/images/" + fileName;
    }
}
