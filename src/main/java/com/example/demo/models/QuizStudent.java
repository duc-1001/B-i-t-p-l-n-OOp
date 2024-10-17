package com.example.demo.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "quiz_students")
public class QuizStudent {
    @Id
    private String id;
    private String classroomId;
    @DBRef
    private Quiz quiz; // ID của bài quiz
    private String studentId; // ID của học sinh
    @DBRef
    private List<QuizResult> quizResults = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setQuizResults(List<QuizResult> quizResults) {
        this.quizResults = quizResults;
    }

    public List<QuizResult> getQuizResults() {
        return quizResults;
    }

    public boolean getIsResultQuiz() {
        for (QuizResult quizResult : this.quizResults) {
            if (quizResult.isCompleted()) {
                return true;
            }
        }
        return false;
    }

    public boolean getIsDoing() {
        if (quizResults == null || quizResults.isEmpty()) {
            return false; 
        }
        QuizResult lastQuizResult = quizResults.get(quizResults.size() - 1);
        return lastQuizResult != null && !lastQuizResult.isCompleted();
    }

}
