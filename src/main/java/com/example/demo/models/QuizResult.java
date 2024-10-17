package com.example.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Document(collection = "quizResult")
public class QuizResult {
    @Id
    private String id;
    private List<String> answers = new ArrayList<>(); // Danh sách câu trả lời
    private double score; // Điểm đạt được
    private int attemptCount; // Lần làm bài thứ mấy
    private boolean completed; // Đã hoàn thành bài thi chưa
    private int attemptTime = 1; // Thời gian làm bài
    private int correctAnswersCount; // Số câu trả lời đúng
    private int wrongAnswersCount; // Số câu trả lời sai
    private int unansweredCount;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getAttemptTime() {
        return attemptTime;
    }

    public void setAttemptTime(int attemptTime) {
        this.attemptTime = attemptTime;
    }

    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    public void setCorrectAnswersCount(int correctAnswersCount) {
        this.correctAnswersCount = correctAnswersCount;
    }

    public int getWrongAnswersCount() {
        return wrongAnswersCount;
    }

    public void setWrongAnswersCount(int wrongAnswersCount) {
        this.wrongAnswersCount = wrongAnswersCount;
    }

    public int getUnansweredCount() {
        return unansweredCount;
    }

    public void setUnansweredCount(int unansweredCount) {
        this.unansweredCount = unansweredCount;
    }

}
