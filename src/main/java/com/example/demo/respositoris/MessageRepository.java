package com.example.demo.respositoris;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.models.Message;
import java.util.*;

public interface MessageRepository extends MongoRepository<Message,String> {
    List<Message> findByClassroomId(String classrommId);
}
