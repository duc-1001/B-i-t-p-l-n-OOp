package com.example.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

import java.util.*;

@Document(collection = "quizResult")
public class QuizResult {
    @Id
    private String id;
    private List<String> answers = new ArrayList<>(); // Danh sách câu trả lời
    private double score; 
    private int attemptCount; 
    private boolean completed; 
    private int attemptTime = 1; 
    private int correctAnswersCount;
    private int wrongAnswersCount; 
    private int unansweredCount;
    private Date submitDate;
    private Date startDate;

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

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getSubmitDateFormat() {
        if (submitDate != null) {
            LocalDateTime localDateTime = submitDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
            return localDateTime.format(dtf);
        }
        return null;
    }

    public String convertSecondsToTimeFormat() {
        int hours = attemptTime / 3600;
        int minutes = (attemptTime % 3600) / 60;
        int seconds = attemptTime % 60;
        List<String> timeParts = new ArrayList<>();
        if (hours > 0) {
            timeParts.add(hours + "h");
        }
        if (minutes > 0) {
            timeParts.add(minutes + "m");
        }
        if (seconds > 0) {
            timeParts.add(seconds + "s");
        }

        return String.join("", timeParts);
    }

}
