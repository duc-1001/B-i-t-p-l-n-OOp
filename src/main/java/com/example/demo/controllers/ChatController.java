package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.example.demo.models.ChatMessage;
import com.example.demo.models.Message;
import com.example.demo.models.User;
import com.example.demo.respositoris.MessageRepository;
import com.example.demo.respositoris.UserRepository;

import java.util.*;

@Controller
public class ChatController {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat/{classroomId}/sendMessage")
    @SendTo("/topic/classroom/{classroomId}/public")
    public Message sendMessage(@Payload ChatMessage chatMessage, @DestinationVariable String classroomId) {
        Message message = new Message();
        String senderId = chatMessage.getSenderId();
        String content= chatMessage.getContent();

        content = content.replace("\n", "<br>");

        Optional<User> optionalSender = userRepository.findById(senderId);
        message.setId(null);
        optionalSender.ifPresent(sender-> message.setSender(sender));
        message.setContent(content);
        message.setClassroomId(classroomId);

        messageRepository.save(message);
        return message;
    }


    @MessageMapping("/chat/{classroomId}/addUser")
    @SendTo("/topic/classroom/{classroomId}/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, @DestinationVariable String classroomId) {
        return chatMessage;
    }

}
