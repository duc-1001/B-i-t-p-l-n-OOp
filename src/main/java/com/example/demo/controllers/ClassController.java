package com.example.demo.controllers;

import jakarta.validation.Valid;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.models.Classroom;
import com.example.demo.models.Question;
import com.example.demo.models.Quiz;
import com.example.demo.models.Quiz.ScoreType;
import com.example.demo.models.QuizResult;
import com.example.demo.models.QuizStudent;
import com.example.demo.models.User;
import com.example.demo.models.UserRegistrationDto;
import com.example.demo.respositoris.ClassroomRepository;
import com.example.demo.respositoris.QuestionRepository;
import com.example.demo.respositoris.QuizRepository;
import com.example.demo.respositoris.QuizStudentRepository;
import com.example.demo.respositoris.UserRepository;

@Controller
@RequestMapping("/classroom")
public class ClassController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuizStudentRepository quizStudentRepository;

    @GetMapping("create")
    public String createClassroom(ModelMap modelMap) {
        modelMap.addAttribute("classroom", new Classroom());
        return "createNewClass";
    }

    @GetMapping("find")
    public String findClassroom(ModelMap modelMap) {
        modelMap.addAttribute("classroom", new Classroom());
        return "findClass";
    }

    @PostMapping("find")
    public String findClassroom(ModelMap modelMap, @ModelAttribute("classroom") Classroom classroom,
            BindingResult result) {
        if (classroom.getId() != null && !classroom.getId().isEmpty()) {
            Optional<Classroom> optionalRoom = classroomRepository.findById(classroom.getId());
            if (optionalRoom.isPresent()) {
                Classroom room = optionalRoom.get();
                modelMap.addAttribute("classroom", room);
                if (room.getTeacherId() != null && !room.getTeacherId().isEmpty()) {
                    Optional<User> optionalTeacher = userRepository.findById(room.getTeacherId());
                    if (optionalTeacher.isPresent()) {
                        User teacher = optionalTeacher.get();
                        modelMap.addAttribute("teacher", teacher);
                    } else {
                        modelMap.addAttribute("error", "Teacher not found.");
                    }
                }
            } else {
                classroom.setId(null);
                modelMap.addAttribute("classroom", classroom);
                result.rejectValue("id", "error.classroom", "Classroom not found.");
            }
        } else {
            result.rejectValue("id", "error.classroom", "Id is required");
        }

        return "findClass";
    }

    @PostMapping("join/{id}")
    public String joinClassroom(ModelMap modelMap, @PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            if ("student".equals(currentUser.getRole())) {
                Optional<Classroom> optionalRoom = classroomRepository.findById(id);
                if (optionalRoom.isPresent()) {
                    Classroom room = optionalRoom.get();
                    List<String> studentIds = room.getStudentIds();
                    if (studentIds == null) {
                        studentIds = new ArrayList<>();
                    }
                    if (!studentIds.contains(currentUser.getId())) {
                        studentIds.add(currentUser.getId());
                        room.setStudentIds(studentIds);
                        classroomRepository.save(room);
                        List<Quiz> quizs = quizRepository.findByClassroom_Id(id);
                        for (Quiz quiz : quizs) {
                            QuizStudent quizStudent = quizStudentRepository.findByStudentIdAndQuiz(currentUser.getId(),
                                    quiz);
                            if (quizStudent == null) {
                                quizStudent = new QuizStudent();
                                quizStudent.setClassroomId(id);
                                quizStudent.setQuiz(quiz);
                                quizStudent.setStudentId(currentUser.getId());
                                quizStudentRepository.save(quizStudent);
                            }

                        }
                    } else {
                        modelMap.addAttribute("error", "You are already joined to this classroom.");
                    }
                }
            }
        }
        return "redirect:/";
    }

    @PostMapping("create")
    public String createClassroom(@Valid @ModelAttribute("classroom") Classroom classroom, BindingResult result) {

        if (result.hasErrors()) {
            return "createNewClass";
        }

        String teacherId = classroom.getTeacherId();
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        if (!"teacher".equals(teacher.getRole())) {
            throw new RuntimeException("Only teachers can create classrooms");
        }
        classroomRepository.save(classroom);

        return "redirect:/";
    }

    @GetMapping("edit/{id}")
    public String editClassroom(@PathVariable("id") String id, ModelMap modelMap) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            Optional<Classroom> optionalClassroom = classroomRepository.findById(id);
            if (optionalClassroom.isPresent()) {
                Classroom classroom = optionalClassroom.get();
                if (!currentUser.getRole().equals("teacher") || !classroom.getTeacherId().equals(currentUser.getId())) {
                    modelMap.addAttribute("errorText", "Bạn không có quyền chỉnh sauwr phòng học này!");
                    return "errorMaxAttempt";
                } else {
                    modelMap.addAttribute("id", classroom.getId());
                    modelMap.addAttribute("name", classroom.getName());
                    modelMap.addAttribute("subject", classroom.getSubjects());
                    return "editClassroom";
                }
            } else {
                modelMap.addAttribute("errorText", "Không tìm thấy phòng học!");
                return "errorMaxAttempt";
            }
        } else {
            return "redirect:/login";
        }
    }

    @PostMapping("edit/{id}")
    public String editClassroom(@PathVariable("id") String id,
            ModelMap modelMap,
            @RequestParam Map<String, String> params) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            Optional<Classroom> optionalClassroom = classroomRepository.findById(id);
            if (optionalClassroom.isPresent()) {
                Classroom classroom = optionalClassroom.get();
                if (!currentUser.getRole().equals("teacher") || !classroom.getTeacherId().equals(currentUser.getId())) {
                    modelMap.addAttribute("errorText", "Bạn không có quyền chỉnh sửa phòng học này!");
                    return "errorMaxAttempt";
                } else {
                    String name = params.get("name");
                    if (name == null || name.isEmpty()) {
                        modelMap.addAttribute("errName", "Tên phòng học không được bỏ trống!");
                        modelMap.addAttribute("id", classroom.getId());
                        modelMap.addAttribute("name", "");
                        modelMap.addAttribute("subject", classroom.getSubjects());
                        return "editClassroom";
                    } else {
                        classroom.setName(name);
                    }
                    String subject = params.get("subject");
                    if (subject == null || subject.isEmpty()) {
                        modelMap.addAttribute("errSubject", "Môn học không được bỏ trống!");
                        modelMap.addAttribute("id", classroom.getId());
                        modelMap.addAttribute("name", classroom.getName());
                        modelMap.addAttribute("subject", "");
                        return "editClassroom";
                    } else {
                        classroom.setSubjects(subject);
                    }
                    classroomRepository.save(classroom);
                    return "redirect:/";
                }
            } else {
                modelMap.addAttribute("errorText", "Không tìm thấy phòng học!");
                return "errorMaxAttempt";
            }
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("delete/{id}")
    public String deleteClass(@PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            if ("teacher".equals(currentUser.getRole())) {
                this.classroomRepository.deleteById(id);
            }
        }
        return "redirect:/";
    }

    @GetMapping("rejoin/{id}")
    public String rejoinClass(@PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            if ("student".equals(currentUser.getRole())) {
                Optional<Classroom> optionalRoom = classroomRepository.findById(id);
                if (optionalRoom.isPresent()) {
                    Classroom room = optionalRoom.get();
                    List<String> studentIds = room.getStudentIds();
                    if (studentIds == null) {
                        studentIds = new ArrayList<>();
                    }
                    if (studentIds.contains(currentUser.getId())) {
                        studentIds.remove(currentUser.getId());
                        room.setStudentIds(studentIds);
                        classroomRepository.save(room);
                    }
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping("{id}/member")
    public String getMemberClassroom(ModelMap modelMap, @PathVariable("id") String id) {
        Optional<Classroom> optionalRoom = classroomRepository.findById(id);
        if (optionalRoom.isPresent()) {
            Classroom room = optionalRoom.get();
            List<String> studentIds = room.getStudentIds();
            List<User> students = new ArrayList<>();
            Optional<User> optionalTeacher = userRepository.findById(room.getTeacherId());
            for (String studentId : studentIds) {
                Optional<User> optionalStudent = userRepository.findById(studentId);
                optionalStudent.ifPresent(students::add);
            }

            modelMap.addAttribute("teacher", optionalTeacher.get());
            modelMap.addAttribute("students", students);
            modelMap.addAttribute("room", room);
        }
        return "classroomMember";
    }

    @GetMapping("{id}/homework/list")
    public String getHomeworkClassroom(ModelMap modelMap, @PathVariable("id") String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            if ("student".equals(currentUser.getRole())) {
                Optional<Classroom> optionalRoom = classroomRepository.findById(id);
                if (optionalRoom.isPresent()) {
                    Classroom room = optionalRoom.get();
                    List<QuizStudent> quizStudents = quizStudentRepository.findByClassroomIdAndStudentId(id,
                            currentUser.getId());
                    Optional<User> optionalTeacher = userRepository.findById(room.getTeacherId());
                    Map<String, Double> scores = new LinkedHashMap<>();
                    for (QuizStudent quizStudent : quizStudents) {
                        scores.put(quizStudent.getId(), 0.0);
                        Quiz quiz = quizStudent.getQuiz();
                        if (quiz.getScoreType().name().equals("FIRST_ATTEMPT")) {
                            List<QuizResult> quizResults = quizStudent.getQuizResults();
                            if (quizResults.size() > 0) {
                                QuizResult quizResult = quizResults.get(0);
                                if (quizResult.isCompleted()) {
                                    scores.put(quizStudent.getId(), quizResult.getScore());
                                }
                            }
                        } else if (quiz.getScoreType().name().equals("LAST_ATTEMPT")) {
                            List<QuizResult> quizResults = quizStudent.getQuizResults();
                            if (quizResults.size() > 0) {
                                for (int i = quizResults.size() - 1; i >= 0; i--) {
                                    if (quizResults.get(i).isCompleted()) {
                                        scores.put(quizStudent.getId(), quizResults.get(i).getScore());
                                        break;
                                    }
                                }
                            }
                        } else if (quiz.getScoreType().name().equals("HIGHEST_SCORE")) {
                            List<QuizResult> quizResults = quizStudent.getQuizResults();
                            double highestScore = 0.0;
                            for (QuizResult quizResult : quizResults) {
                                if (quizResult.isCompleted() && quizResult.getScore() > highestScore) {
                                    highestScore = quizResult.getScore();
                                }
                            }
                            scores.put(quizStudent.getId(), highestScore);
                        }
                    }
                    modelMap.addAttribute("teacher",optionalTeacher.get());
                    modelMap.addAttribute("quizStudents", quizStudents);
                    modelMap.addAttribute("scores", scores);
                    modelMap.addAttribute("room", room);
                }
                return "classroomHomeworkStudent";
            } else {
                Optional<Classroom> optionalRoom = classroomRepository.findById(id);
                if (optionalRoom.isPresent()) {
                    Classroom room = optionalRoom.get();
                    List<Quiz> quizzes = quizRepository.findByClassroom_Id(id);
                    Map<String, Integer> count = new LinkedHashMap<>();
                    for (Quiz quiz : quizzes) {
                        List<QuizStudent> quizStudents = quizStudentRepository.findByClassroomIdAndQuiz(id, quiz);
                        count.put(quiz.getId(), 0);
                        for (QuizStudent quizStudent : quizStudents) {
                            if (quizStudent.getQuizResults().size() > 0) {
                                count.put(quiz.getId(), count.get(quiz.getId()) + 1);
                            }
                        }
                    }

                    modelMap.addAttribute("quizzes", quizzes);
                    modelMap.addAttribute("count", count);
                    modelMap.addAttribute("room", room);
                }
                return "classroomHomework";
            }
        } else {
            return "error";
        }
    }

    @GetMapping("delete_user/{id}/{classId}")
    public String deleteUserClassroom(ModelMap modelMap, @PathVariable("id") String studentId,
            @PathVariable("classId") String classId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User currentUser = userRepository.findByEmail(username);
            if ("teacher".equals(currentUser.getRole())) {
                Optional<Classroom> optionalRoom = classroomRepository.findById(classId);
                if (optionalRoom.isPresent()) {
                    Classroom room = optionalRoom.get();
                    List<String> studentIds = room.getStudentIds();
                    if (studentIds == null) {
                        studentIds = new ArrayList<>();
                    }
                    if (studentIds.contains(studentId)) { // Kiểm tra xem sinh viên có trong danh sách hay không
                        studentIds.remove(studentId); // Xóa sinh viên khỏi danh sách
                        room.setStudentIds(studentIds); // Cập nhật danh sách sinh viên
                        classroomRepository.save(room); // Lưu lại phòng học
                    } else {
                        modelMap.addAttribute("error", "Student is not a member of this classroom.");
                    }
                }
            }
        }
        return "redirect:/classroom/" + classId + "/member";
    }

    @GetMapping("{id}/homework/add")
    public String createHomework(ModelMap modelMap, @PathVariable("id") String id) {
        modelMap.addAttribute("quiz", new Quiz());
        modelMap.addAttribute("id", id);
        return "createHomework";
    }

    @PostMapping("{id}/homework/add")
    public String createQuiz(@ModelAttribute Quiz quiz,
            @RequestParam Map<String, String> params,
            @PathVariable("id") String id) {

        Optional<Classroom> optionalRoom = classroomRepository.findById(id);
        System.out.println(params);
        if (optionalRoom.isPresent()) {
            Classroom room = optionalRoom.get();
            List<Question> questionList = new ArrayList<>();
            int i = 0;
            while (params.containsKey("questions[" + i + "].questionText")) {
                Question question = new Question();
                question.setQuestionText(params.get("questions[" + i + "].questionText"));

                List<String> options = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    String option = params.get("questions[" + i + "].options[" + j + "]");
                    if (option != null) {
                        options.add(option);
                    }
                }
                question.setOptions(options);
                question.setCorrectAnswer(params.get("questions[" + i + "].correctAnswer"));
                questionRepository.save(question);
                questionList.add(question);
                i++;
            }

            int exerciseQuiz = room.getExercise();
            room.setExercise(exerciseQuiz + 1);
            classroomRepository.save(room);

            quiz.setId(null);
            quiz.setClassroom(room);
            quiz.setQuestions(questionList);

            if (params.get("examDurationCheckBox") != null) {
                quiz.setIsExamDuration(true);
                quiz.setExamDuration(Integer.parseInt(params.get("examDuration")));
            }

            if (params.get("showAnswer") != null) {
                quiz.setShowAnswer(true);
            }

            if (params.containsKey("scoreType")) {
                ScoreType scoreType = ScoreType.valueOf(params.get("scoreType"));
                quiz.setScoreType(scoreType);
            } else {
                quiz.setScoreType(ScoreType.FIRST_ATTEMPT);
            }
            quizRepository.save(quiz);

            List<String> studentIds = room.getStudentIds();
            for (String studentId : studentIds) {
                QuizStudent quizStudent = new QuizStudent();
                quizStudent.setId(null);
                quizStudent.setClassroomId(id);
                quizStudent.setQuiz(quiz);
                quizStudent.setStudentId(studentId);
                quizStudentRepository.save(quizStudent);
            }
            return "redirect:/classroom/" + id + "/homework/list";
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping("{id}/homework/edit/{quizId}")
    public String editHomework(@PathVariable("id") String id, @PathVariable("quizId") String quizId,
            ModelMap modelMap) {
        Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);
        if (optionalQuiz.isPresent()) {
            Quiz quiz = optionalQuiz.get();
            List<Question> questions = quiz.getQuestions();
            modelMap.addAttribute("id", id);
            modelMap.addAttribute("quiz", quiz);
            modelMap.addAttribute("listQuestion", questions);
            return "editHomework";
        } else {
            return "redirect:/error";
        }
    }

    @PostMapping("{id}/homework/edit/{quizId}")
    public String editQuiz(@PathVariable("id") String id,
            @PathVariable("quizId") String quizId,
            @ModelAttribute Quiz quiz,
            @RequestParam Map<String, String> params) {

        Optional<Classroom> optionalRoom = classroomRepository.findById(id);
        Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

        if (optionalRoom.isPresent() && optionalQuiz.isPresent()) {
            Classroom room = optionalRoom.get();
            Quiz existingQuiz = optionalQuiz.get();

            existingQuiz.setTitle(quiz.getTitle());
            existingQuiz.setMaxAttempts(quiz.getMaxAttempts());

            if (params.get("showAnswer") != null) {
                existingQuiz.setShowAnswer(true);
            } else {
                existingQuiz.setShowAnswer(false);
            }

            if (params.get("examDurationCheckBox") != null) {
                existingQuiz.setIsExamDuration(true);
                existingQuiz.setExamDuration(Integer.parseInt(params.get("examDuration")));
            } else {
                existingQuiz.setIsExamDuration(false);
            }

            if (params.containsKey("scoreType")) {
                ScoreType scoreType = ScoreType.valueOf(params.get("scoreType"));
                existingQuiz.setScoreType(scoreType);
            } else {
                existingQuiz.setScoreType(ScoreType.FIRST_ATTEMPT);
            }
            List<Question> questionList = new ArrayList<>();
            int i = 0;

            while (params.containsKey("questions[" + i + "].questionText")) {

                Question question;
                if (i < existingQuiz.getQuestions().size()) {
                    question = existingQuiz.getQuestions().get(i);
                } else {
                    question = new Question();
                }

                question.setQuestionText(params.get("questions[" + i + "].questionText"));

                List<String> options = new ArrayList<>();
                for (int j = 0; j < 4; j++) {
                    String option = params.get("questions[" + i + "].options[" + j + "]");
                    if (option != null) {
                        options.add(option);
                    }
                }
                question.setOptions(options);
                question.setCorrectAnswer(params.get("questions[" + i + "].correctAnswer"));

                questionList.add(question);
                questionRepository.save(question);
                i++;
            }

            if (i < existingQuiz.getQuestions().size()) {
                for (int j = i; j < existingQuiz.getQuestions().size(); j++) {
                    Question questionToDelete = existingQuiz.getQuestions().get(j);
                    questionRepository.delete(questionToDelete);
                }
            }

            existingQuiz.setQuestions(questionList); // cập nhật danh sách câu hỏi
            quizRepository.save(existingQuiz); // lưu quiz mới
        }
        return "redirect:/classroom/" + id + "/homework/list";
    }

}
