package com.example.demo.respositoris;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.models.QuizStudent;
import com.example.demo.models.Quiz;
import java.util.*;

public interface QuizStudentRepository extends MongoRepository<QuizStudent,String> {
    List<QuizStudent> findByStudentId(String studentId);
    List<QuizStudent> findByClassroomId(String classrommId);
    List<QuizStudent> findByClassroomIdAndStudentId(String classroomId, String studentId);
    QuizStudent findByClassroomIdAndStudentIdAndQuiz(String classroomId, String studentId,Quiz quiz);
    QuizStudent findByStudentIdAndQuiz(String studentId,Quiz quiz);
    List<QuizStudent> findByClassroomIdAndQuiz(String classroomId,Quiz quiz);
}
