package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.example.demo.modelsDTO.QuizDetalDTO;
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
                                quizResult.setStartDate(new Date());
                                quizResult.setSubmitDate(new Date());
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
            System.out.println("Đã nộp bài");
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
                    quizResult.setCompleted(true);
                    quizResult.setAnswers(answers);
                    quizResult.setScore(roundedScore.doubleValue());
                    quizResult.setCorrectAnswersCount(correctAnswersCount);
                    quizResult.setWrongAnswersCount(wrongAnswersCount);
                    quizResult.setUnansweredCount(unansweredCount);
                    quizResult.setSubmitDate(new Date());
                    quizResultRepository.save(quizResult);
                }

                modelMap.addAttribute("quiz", quiz);
                modelMap.addAttribute("questions", questions);
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
                    quizResult.setSubmitDate(new Date());
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
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            Optional<Quiz> optionalQuiz = quizRepository.findById(id);
            if (optionalQuiz.isPresent()) {
                Quiz quiz = optionalQuiz.get();
                List<Question> questions = quiz.getQuestions();
                Classroom classroom = quiz.getClassroom();
                if ("student".equals(currentUser.getRole())) {
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
                    modelMap.addAttribute("quizResults", quizResults);
                    modelMap.addAttribute("classroom", classroom);
                    optionalQuizResult.ifPresent(quizResult -> {
                        modelMap.addAttribute("answers", quizResult.getAnswers());
                        modelMap.addAttribute("quizResult", quizResult);
                    });
                    modelMap.addAttribute("questions", questions);
                    return "detalQuiz";
                } else {
                    if (!classroom.getTeacherId().equals(currentUser.getId())) {
                        modelMap.addAttribute("errorText", "Không có quyền xem");
                        return "errorMaxAttempt";
                    } else {
                        List<String> studentIds = classroom.getStudentIds();
                        ArrayList<QuizStudent> quizStudents = new ArrayList<>();
                        ArrayList<QuizDetalDTO> quizDetalDTOs = new ArrayList<>();
                        Map<Integer, Integer> scoreMap = new LinkedHashMap<>();
                        int count = 0;
                        for (int i = 1; i <= 10; i++) {
                            scoreMap.put(i, 0);
                        }
                        for (String studentId : studentIds) {
                            QuizDetalDTO quizDetalDTO = new QuizDetalDTO();
                            Optional<User> optionalStudent = userRepository.findById(studentId);
                            optionalStudent.ifPresent(student -> quizDetalDTO.setUser(student));
                            QuizStudent quizStudent = quizStudentRepository
                                    .findByClassroomIdAndStudentIdAndQuiz(classroom.getId(), studentId, quiz);
                            List<QuizResult> quizResults = quizStudent.getQuizResults();
                            if (quizResults.size() > 0 && quizResults.get(0).isCompleted()) {
                                count += 1;
                            }
                            if (quizResults.size() == 0) {
                                quizDetalDTO.setAttempt(0);
                                quizDetalDTO.setQuizResult(null);
                            } else if (quizResults.size() == 1) {
                                quizDetalDTO.setQuizResult(quizResults.get(0));
                                if (quizResults.get(0).isCompleted()) {
                                    quizDetalDTO.setAttempt(1);
                                } else {
                                    quizDetalDTO.setAttempt(0);
                                }
                            } else {
                                if (quizResults.get(quizResults.size() - 1).isCompleted()) {
                                    quizDetalDTO.setAttempt(quizResults.size());
                                } else {
                                    quizDetalDTO.setAttempt(quizResults.size() - 1);
                                }
                                if (quiz.getScoreType().name() == "FIRST_ATTEMPT") {
                                    quizDetalDTO.setQuizResult(quizResults.get(0));
                                } else if (quiz.getScoreType().name() == "LAST_ATTEMPT") {
                                    for (int i = quizResults.size() - 1; i >= 0; i--) {
                                        if (quizResults.get(i).isCompleted()) {
                                            quizDetalDTO.setQuizResult(quizResults.get(i));
                                            break;
                                        }
                                    }
                                } else {
                                    double highestScore = 0.0;
                                    QuizResult quizResultUser = null;
                                    for (QuizResult quizResult : quizResults) {
                                        if (quizResult.isCompleted() && quizResult.getScore() > highestScore) {
                                            highestScore = quizResult.getScore();
                                            quizResultUser = quizResult;
                                        }
                                    }
                                    quizDetalDTO.setQuizResult(quizResultUser);
                                }
                            }

                            if (quizDetalDTO.getQuizResult() != null && quizDetalDTO.getQuizResult().isCompleted()) {
                                double score = quizDetalDTO.getQuizResult().getScore();
                                for (int i = 1; i <= 10; i++) {
                                    if (score <= i && score >= i - 1) {
                                        scoreMap.put(i, scoreMap.get(i) + 1);
                                    }
                                }
                            }
                            quizDetalDTOs.add(quizDetalDTO);
                        }

                        for (Integer key : scoreMap.keySet()) {
                            System.out.println("Key: " + key + ", Value: " + scoreMap.get(key));
                        }
                        modelMap.addAttribute("quizDetalDTOs", quizDetalDTOs);
                        modelMap.addAttribute("scoreMap", scoreMap);
                        modelMap.addAttribute("count", count);
                        modelMap.addAttribute("quiz", quiz);
                        modelMap.addAttribute("questions", quiz.getQuestions());
                        modelMap.addAttribute("classroom", classroom);
                        return "detalQuizTeacher";
                    }
                }
            } else {
                modelMap.addAttribute("errorText", "Không tìm thấy bài tập này!");
                return "errorMaxAttempt";
            }
        } else {
            return "redirect:/login";
        }
    }
}
