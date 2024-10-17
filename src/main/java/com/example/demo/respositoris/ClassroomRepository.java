package com.example.demo.respositoris;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.models.Classroom;
import java.util.*;

@Repository
public interface ClassroomRepository extends MongoRepository<Classroom, String> {
    List<Classroom> findByTeacherId(String teacherId);
    List<Classroom> findByName(String name);
    List<Classroom> findByStudentIdsContaining(String userId);
}
