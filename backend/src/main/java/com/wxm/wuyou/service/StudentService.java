package com.wxm.wuyou.service;

import com.wxm.wuyou.entity.Student;
import com.wxm.wuyou.repository.StudentRepository;
import com.wxm.wuyou.utils.PhoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    // 获取所有学生的列表
    public List<Student> getAllStudents() {
        // 记录日志信息，表示正在获取所有学生
        logger.info("Fetching all students");
        // 调用studentRepository的findAll方法，返回所有学生的列表
        return studentRepository.findAll();
    }

    // 根据学生ID获取学生信息
    public Optional<Student> getStudentById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid student ID: " + id);
        }
        logger.info("Fetching student with ID: {}", id);
        return studentRepository.findById(id);
    }

    // 保存学生信息
    @Transactional
    public Student saveStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student object cannot be null");
        }
        logger.info("Saving new student: {}", student);
        return studentRepository.save(student);
    }

    // 更新学生信息
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        // 检查学生ID是否有效，ID不能为null且必须大于0
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid student ID: " + id);
        }
        // 检查学生详细信息是否为null
        if (studentDetails == null) {
            throw new IllegalArgumentException("Student details cannot be null");
        }

        // 记录日志，表示正在更新指定ID的学生信息
        logger.info("Updating student with ID: {}", id);
        // 根据ID查找学生信息
        Student student = findStudentById(id);

        // 更新学生的年龄
        student.setAge(studentDetails.getAge());
        // 更新学生的性别
        student.setGender(studentDetails.getGender());
        // 更新学生的专业
        student.setMajor(studentDetails.getMajor());
        // 更新学生的年级
        student.setGrade(studentDetails.getGrade());
        // 更新学生的每月预期支出
        student.setExpectMonthlySpend(studentDetails.getExpectMonthlySpend());
        // 将更新后的学生信息保存到数据库并返回
        return studentRepository.save(student);
    }

    // 根据学生ID删除学生信息
    @Transactional // 标记该方法为事务性操作，确保数据一致性
    public void deleteStudent(Long id) { // 定义一个方法，用于删除学生信息，参数为学生ID
        if (id == null || id <= 0) { // 检查传入的学生ID是否为空或小于等于0
            throw new IllegalArgumentException("Invalid student ID: " + id); // 如果ID无效，抛出非法参数异常
        }

        logger.info("Deleting student with ID: {}", id); // 记录日志，表示正在删除指定ID的学生
        findStudentById(id); // 确保学生存在
        studentRepository.deleteById(id);
    }

    // 私有方法：根据ID查找学生，若不存在则抛出自定义异常
     Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));
    }

    public List<Student> findStudentByName(String name) {
        return studentRepository.findByName(name);
    }
}

// 自定义异常类
// 继承自RuntimeException，表示这是一个运行时异常
class StudentNotFoundException extends RuntimeException {
    // 构造方法，用于创建StudentNotFoundException对象
    // 接受一个String类型的参数message，用于传递异常信息
    public StudentNotFoundException(String message) {
        // 调用父类RuntimeException的构造方法，将异常信息传递给父类
        super(message);
    }
}
