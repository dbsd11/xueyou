package group.bison.xueyou.controller;

import group.bison.xueyou.common.response.ResponseResult;
import group.bison.xueyou.entity.Student;
import group.bison.xueyou.interceptor.AuthInterceptor;
import group.bison.xueyou.service.StudentService;
import group.bison.xueyou.utils.PhoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    // 定义日志对象，用于记录日志信息
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    // 自动注入StudentService，用于处理学生相关的业务逻辑
    @Autowired
    private StudentService studentService;

    @GetMapping("/current")
    public ResponseEntity<ResponseResult> getCurrentStudentInfo(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone) {
        List<Student> studentList = studentService.findStudentByName(PhoneUtil.encrypt(phone));
        Student matchStudent = studentList.stream().findFirst().orElse(null);
        return ResponseEntity.ok(new ResponseResult(matchStudent));
    }

    /**
     * 创建新的学生信息
     */
    @PostMapping("/upsert")
    public ResponseEntity upsertStudent(@RequestAttribute(AuthInterceptor.ATTRIBUTE_PHONE) String phone, @RequestBody Student student) {
    // 检查传入的学生对象是否为空
        if (student == null) {
        // 记录警告日志，说明创建学生信息失败，原因是传入对象为空
            logger.warn("创建学生信息失败：传入对象为空");
            return ResponseEntity.badRequest().body(new ResponseResult<>(1, "创建学生信息失败：传入对象为空")); // 返回400 Bad Request
        }
        try {
            student.setName(PhoneUtil.encrypt(phone));
            Student createdStudent = studentService.saveStudent(student);
            return ResponseEntity.ok(new ResponseResult<>(createdStudent));
        } catch (Exception e) {
            logger.error("创建学生信息失败", e);
            return ResponseEntity.status(500).body(new ResponseResult<>(1, e.getMessage())); // 返回500 Internal Server Error
        }
    }
}
