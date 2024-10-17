package com.example.demo.respositoris;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.models.Classroom;
import com.example.demo.models.Quiz;
import com.example.demo.models.QuizResult;

import java.util.*;

@Repository
public interface QuizResultRepository extends MongoRepository<QuizResult, String> {

}
