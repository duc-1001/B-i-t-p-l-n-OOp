package com.example.demo.respositoris;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.models.Classroom;
import com.example.demo.models.Quiz;

import java.util.*;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findByClassroomId(String classroomId);
    List<Quiz> findByClassroom_Id(String classroomId);
}
