package com.example.demo.modelsDTO;

import com.example.demo.models.QuizResult;
import com.example.demo.models.User;

public class QuizDetalDTO {
    private User user;
    private QuizResult quizResult;
    private int attempt;

    public void setQuizResult(QuizResult quizResult) {
        this.quizResult = quizResult;
    }

    public QuizResult getQuizResult() {
        return quizResult;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getAttempt() {
        return attempt;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
