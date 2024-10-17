package com.example.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

import java.time.Duration;
import java.util.*;

@Document(collection = "classrooms")
public class Classroom {
    @Id
    private String id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Subjects is required")
    private String subjects;
    private String teacherId;
    private List<String> studentIds = new ArrayList<>();
    private int exercise;
    public Classroom() {
    }

    public Classroom(String name,String subjects, String teacherId) {
        this.name = name;
        this.subjects = subjects;
        this.teacherId = teacherId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }

    public int getExercise() {
        return exercise;
    }

    public void setExercise(int exercise) {
        this.exercise = exercise;
    }

    public Integer getStudentsCount() {
        return this.studentIds.size();
    }
    @Override
    public String toString() {
        return "Classroom{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", name='" + subjects + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", studentIds=" + studentIds +
                ", exercise=" + exercise +
                '}';
    }
}
