package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.models.Classroom;
import com.example.demo.models.Question;
import com.example.demo.models.Quiz;
import com.example.demo.models.QuizResult;
import com.example.demo.models.QuizStudent;
import com.example.demo.models.User;
import com.example.demo.respositoris.ClassroomRepository;
import com.example.demo.respositoris.QuestionRepository;
import com.example.demo.respositoris.QuizRepository;
import com.example.demo.respositoris.QuizResultRepository;
import com.example.demo.respositoris.QuizStudentRepository;
import com.example.demo.respositoris.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuizStudentRepository quizStudentRepository;
    @Autowired
    private QuizResultRepository quizResultRepository;

    @GetMapping("/{id}")
    public String getQuiz(ModelMap modelMap, @PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            if (currentUser != null && "student".equals(currentUser.getRole())) {
                Optional<Quiz> optionalQuiz = quizRepository.findById(id);
                if (optionalQuiz.isPresent()) {
                    Quiz quiz = optionalQuiz.get();
                    List<Question> questions = quiz.getQuestions();
                    Classroom classroom = quiz.getClassroom();
                    QuizStudent quizStudent = quizStudentRepository.findByClassroomIdAndStudentIdAndQuiz(
                            classroom.getId(), currentUser.getId(), quiz);
                    List<QuizResult> quizResults = quizStudent.getQuizResults();
                    if (quizStudent != null) {
                        int len = quizResults.size();
                        if (len >= quiz.getMaxAttempts()) {
                            modelMap.addAttribute("errorText", "Đã quá số lần làm bài cho phép");
                            return "errorMaxAttempt";
                        } else {
                            QuizResult lastQuizResult = len > 0 ? quizResults.get(len - 1) : null;
                            if (lastQuizResult == null || lastQuizResult.isCompleted()) {
                                List<String> answers = new ArrayList<>();
                                for (Question question : questions) {
                                    String answer = "";
                                    answers.add(answer);
                                }
                                System.out.println("mới");
                                QuizResult quizResult = new QuizResult();
                                quizResult.setCompleted(false);
                                quizResult.setAnswers(answers);
                                quizResultRepository.save(quizResult);
                                quizResults.add(quizResult);
                                quizStudent.setQuizResults(quizResults);
                                quizStudentRepository.save(quizStudent);
                                modelMap.addAttribute("quizResultId", quizResult.getId());
                                modelMap.addAttribute("quizResult", quizResult);
                            } else {
                                System.out.println("cũ");
                                modelMap.addAttribute("quizResultId", lastQuizResult.getId());
                                modelMap.addAttribute("quizResult", lastQuizResult);
                            }
                            modelMap.addAttribute("quiz", quiz);
                            modelMap.addAttribute("questions", questions);
                            return "quiz";
                        }
                    } else {
                        modelMap.addAttribute("errorText", "Không tìm thấy thông tin của bạn trong bài thi này");
                        return "error";
                    }
                } else {
                    modelMap.addAttribute("errorText", "Bài thi không tồn tại");
                    return "error";
                }
            } else {
                modelMap.addAttribute("errorText", "Giáo viên không có quyền làm bài");
                return "errorMaxAttempt";
            }
        } else {
            modelMap.addAttribute("errorText", "Bạn cần đăng nhập để làm bài");
            return "error";
        }
    }

    @PostMapping("/{id}")
    public String getQuiz(@PathVariable("id") String id, @RequestParam Map<String, String> params, ModelMap modelMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            Optional<Quiz> optionalQuiz = quizRepository.findById(id);
            if (optionalQuiz.isPresent()) {
                Quiz quiz = optionalQuiz.get();
                List<Question> questions = quiz.getQuestions();
                Classroom classroom = quiz.getClassroom();
                QuizStudent quizStudent = quizStudentRepository.findByClassroomIdAndStudentIdAndQuiz(classroom.getId(),
                        currentUser.getId(), quiz);

                int correctAnswersCount = 0;
                int wrongAnswersCount = 0;
                int unansweredCount = 0;
                int len = quiz.getQuestions().size();
                List<String> answers = new ArrayList<>();
                for (Question question : questions) {
                    String answer = params.get(question.getId()) != null ? params.get(question.getId()) : "";
                    if (answer.isEmpty()) {
                        unansweredCount++;
                    } else if (answer.equals(question.getCorrectAnswer())) {
                        correctAnswersCount++;
                    } else {
                        wrongAnswersCount++;
                    }
                    answers.add(answer);
                }
                double score = (double) correctAnswersCount / len * 10;
                BigDecimal roundedScore = BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
                Optional<QuizResult> optionalQuizResult = quizResultRepository.findById(params.get("quizResultId"));
                List<QuizResult> quizResults = quizStudent.getQuizResults();
                if (quizResults == null) {
                    quizResults = new ArrayList<>();
                }
                if (optionalQuizResult.isPresent()) {
                    QuizResult quizResult = optionalQuizResult.get();
                    System.out.println(quizResult.getId());
                    quizResult.setCompleted(true);
                    quizResult.setAnswers(answers);
                    quizResult.setScore(roundedScore.doubleValue());
                    quizResult.setCorrectAnswersCount(correctAnswersCount);
                    quizResult.setWrongAnswersCount(wrongAnswersCount);
                    quizResult.setUnansweredCount(unansweredCount);
                    quizResultRepository.save(quizResult);
                }

                modelMap.addAttribute("quiz", quiz);
                modelMap.addAttribute("questions", questions);
                System.out.println(roundedScore);
                return "redirect:/quiz/" + id + "/detal?q=" + params.get("quizResultId");

            } else {
                return "error";
            }
        } else {
            return "error";
        }
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<Void> saveQuiz(@PathVariable("id") String id, @RequestBody Map<String, String> params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            Optional<Quiz> optionalQuiz = quizRepository.findById(id);

            if (optionalQuiz.isPresent()) {
                Quiz quiz = optionalQuiz.get();
                List<Question> questions = quiz.getQuestions();
                Classroom classroom = quiz.getClassroom();
                QuizStudent quizStudent = quizStudentRepository.findByClassroomIdAndStudentIdAndQuiz(classroom.getId(),
                        currentUser.getId(), quiz);

                List<String> answers = new ArrayList<>();
                for (Question question : questions) {
                    String answer = params.get(question.getId()) != null ? params.get(question.getId()) : "";
                    answers.add(answer);
                }
                Optional<QuizResult> optionalQuizResult = quizResultRepository.findById(params.get("quizResultId"));
                List<QuizResult> quizResults = quizStudent.getQuizResults();
                if (quizResults == null) {
                    quizResults = new ArrayList<>();
                }
                if (optionalQuizResult.isPresent()) {
                    QuizResult quizResult = optionalQuizResult.get();
                    quizResult.setAnswers(answers);
                    quizResult.setAttemptTime(Integer.parseInt(params.get("timeElapsed")));
                    quizResultRepository.save(quizResult);
                }
                return ResponseEntity.noContent().build();
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{id}/detal")
    public String getDetalQuiz(@PathVariable("id") String id, ModelMap modelMap,
            @RequestParam Map<String, String> params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // int q = Integer.parseInt(params.get("q"));
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            if ("student".equals(currentUser.getRole())) {
                Optional<Quiz> optionalQuiz = quizRepository.findById(id);
                if (optionalQuiz.isPresent()) {
                    Quiz quiz = optionalQuiz.get();
                    List<Question> questions = quiz.getQuestions();
                    Classroom classroom = quiz.getClassroom();
                    QuizStudent quizStudent = quizStudentRepository.findByClassroomIdAndStudentIdAndQuiz(
                            classroom.getId(),
                            currentUser.getId(), quiz);
                    List<QuizResult> quizResults = new ArrayList<>();
                    for (QuizResult quizResult : quizStudent.getQuizResults()) {
                        if (quizResult.isCompleted()) {
                            quizResults.add(quizResult);
                        }
                    }
                    if (quiz.getShowAnswer()) {
                        List<String> correctAnswers = new ArrayList<>();
                        for (Question question : questions) {
                            correctAnswers.add(question.getCorrectAnswer());
                        }
                        modelMap.addAttribute("correctAnswers", correctAnswers);
                    }
                    Optional<QuizResult> optionalQuizResult = quizResultRepository.findById(params.get("q"));
                    int index = -1;
                    for (int i = 0; i < quizResults.size(); i++) {
                        if (quizResults.get(i).getId().equals(params.get("q"))) {
                            index = i + 1;
                            break;
                        }
                    }
                    if (index == -1) {
                        modelMap.addAttribute("errorText", "Không có kết quả bài làm");
                        return "errorMaxAttempt";
                    }
                    modelMap.addAttribute("q", index);
                    modelMap.addAttribute("quiz", quiz);
                    modelMap.addAttribute("classroom", classroom);
                    modelMap.addAttribute("quizResults", quizResults);
                    optionalQuizResult.ifPresent(quizResult -> {
                        modelMap.addAttribute("answers", quizResult.getAnswers());
                        modelMap.addAttribute("quizResult", quizResult);
                    });
                    modelMap.addAttribute("questions", questions);
                }
                return "detalQuiz";
            } else {
                modelMap.addAttribute("errorText", "Không có quyền xem");
                return "errorMaxAttempt";
            }
        } else {
            return "redirect:/login";
        }
    }
}
