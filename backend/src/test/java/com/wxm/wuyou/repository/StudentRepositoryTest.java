package com.wxm.wuyou.repository;

import com.wxm.wuyou.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudentRepositoryTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentRepositoryTest studentRepositoryTest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findByName_MultipleStudentsWithSameName_ReturnsAllStudents() {
        String name = "John Doe";
        Student student1 = new Student();
        student1.setId(1L);
        student1.setName(name); // 此处不再报错
        Student student2 = new Student();
        student2.setId(2L);
        student2.setName(name);

        List<Student> expectedStudents = Arrays.asList(student1, student2);

        when(studentRepository.findByName(name)).thenReturn(expectedStudents);

        List<Student> actualStudents = studentRepository.findByName(name);

        assertEquals(expectedStudents, actualStudents);
    }

    @Test
    public void findByName_NoStudentsWithSameName_ReturnsEmptyList() {
        String name = "Jane Doe";

        when(studentRepository.findByName(name)).thenReturn(Arrays.asList());

        List<Student> actualStudents = studentRepository.findByName(name);

        assertTrue(actualStudents.isEmpty());
    }

    @Test
    public void findById_StudentFound_ReturnsStudent() {
        Long id = 1L;
        Student expectedStudent = new Student();
        expectedStudent.setId(id);
        expectedStudent.setName("John Doe");

        when(studentRepository.findById(id)).thenReturn(Optional.of(expectedStudent));

        Optional<Student> actualStudent = studentRepository.findById(id);

        assertTrue(actualStudent.isPresent());
        assertEquals(expectedStudent, actualStudent.get());
    }

    @Test
    public void findById_StudentNotFound_ReturnsEmptyOptional() {
        Long id = 2L;

        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Student> actualStudent = studentRepository.findById(id);

        assertFalse(actualStudent.isPresent());
    }

    @Test
    public void findByAgeRange_StudentsInAgeRange_ReturnsStudents() {
        int minAge = 18;
        int maxAge = 25;
        Student student1 = new Student();
        student1.setId(1L);
        student1.setAge(20);
        Student student2 = new Student();
        student2.setId(2L);
        student2.setAge(22);

        List<Student> expectedStudents = Arrays.asList(student1, student2);

        when(studentRepository.findByAgeRange(minAge, maxAge)).thenReturn(expectedStudents);

        List<Student> actualStudents = studentRepository.findByAgeRange(minAge, maxAge);

        assertEquals(expectedStudents, actualStudents);
    }

    @Test
    public void findByAgeRange_NoStudentsInAgeRange_ReturnsEmptyList() {
        int minAge = 30;
        int maxAge = 40;

        when(studentRepository.findByAgeRange(minAge, maxAge)).thenReturn(Arrays.asList());

        List<Student> actualStudents = studentRepository.findByAgeRange(minAge, maxAge);

        assertTrue(actualStudents.isEmpty());
    }
}
