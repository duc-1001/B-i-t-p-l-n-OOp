package com.example.demo.respositoris;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.models.Question;
import com.example.demo.models.Quiz;
import java.util.*;

public interface QuestionRepository extends MongoRepository<Question,String> {

}
