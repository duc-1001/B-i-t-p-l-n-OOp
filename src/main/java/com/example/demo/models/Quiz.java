package com.example.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

@Document(collection = "quizzes")
public class Quiz {
    @Id
    private String id;
    private String title;
    @Size(max = 20)
    private int maxAttempts;
    private List<String> studentIds = new ArrayList<>();
    private boolean showAnswer = false;
    @DBRef
    private Classroom classroom;
    @DBRef
    private List<Question> questions;
    private int examDuration;
    private boolean isExamDuration = false;

    private ScoreType scoreType = ScoreType.FIRST_ATTEMPT;

    public Quiz() {
    }

    public Quiz(String id, String title, int maxAttempts, boolean showAnswer, Classroom classroom,
            List<Question> questions, ScoreType scoreType) {
        this.id = id;
        this.title = title;
        this.maxAttempts = maxAttempts;
        this.showAnswer = showAnswer;
        this.classroom = classroom;
        this.questions = questions;
        this.scoreType = scoreType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }

    public boolean getShowAnswer() {
        return showAnswer;
    }

    public void setShowAnswer(boolean showAnswer) {
        this.showAnswer = showAnswer;
    }
    
    public boolean getIsExamDuration() {
        return isExamDuration;
    }

    public void setIsExamDuration(boolean isExamDuration) {
        this.isExamDuration = isExamDuration;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public ScoreType getScoreType() {
        return scoreType;
    }

    public void setScoreType(ScoreType scoreType) {
        this.scoreType = scoreType;
    }

    public int getExamDuration() {
        return examDuration;
    }

    public void setExamDuration(int examDuration) {
        this.examDuration = examDuration;
    }

    public enum ScoreType {
        FIRST_ATTEMPT,
        LAST_ATTEMPT,
        HIGHEST_SCORE
    }
}
