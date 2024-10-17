package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.models.Classroom;
import com.example.demo.models.User;
import com.example.demo.models.UserRegistrationDto;
import com.example.demo.respositoris.ClassroomRepository;
import com.example.demo.respositoris.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/")
public class HomeControllers {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home(ModelMap modelMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByEmail(username);
        if ("teacher".equals(currentUser.getRole())) {
            List<Classroom> rooms = classroomRepository.findByTeacherId(currentUser.getId());
            modelMap.addAttribute("currentUser", currentUser);
            modelMap.addAttribute("rooms", rooms);
        } else {
            List<Classroom> rooms = classroomRepository.findByStudentIdsContaining(currentUser.getId());
            modelMap.addAttribute("currentUser", currentUser);
            modelMap.addAttribute("rooms", rooms);
        }
        return "home";
    }

    @GetMapping("/register")
    public String register(ModelMap modelMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            return "redirect:/";
        }
        modelMap.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @GetMapping("/login")
    public String login(ModelMap modelMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String)) {
            return "redirect:/"; // Điều hướng đến trang chính nếu đã đăng nhập
        }
        modelMap.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
            BindingResult result,
            ModelMap modelMap) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
        }
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }
        if (result.hasErrors()) {
            return "register";
        }
        User user = new User(userDto.getUsername(), userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()),
                userDto.getRole().toLowerCase());
        userRepository.save(user);
        return "redirect:/login";
    }

}
