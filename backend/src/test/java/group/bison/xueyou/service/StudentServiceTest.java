package group.bison.xueyou.service;

import group.bison.xueyou.entity.Student;
import group.bison.xueyou.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    public void setUp() {
        student = new Student();
        student.setId(1L);
        student.setName("John Doe");
        student.setAge(20);
        student.setGender("Male");
        student.setMajor("Computer Science");
        student.setGrade("Sophomore");
    }

    @Test
    public void getAllStudents_ReturnsAllStudents() {
        List<Student> students = Arrays.asList(student);
        when(studentRepository.findAll()).thenReturn(students);

        List<Student> result = studentService.getAllStudents();

        assertEquals(students, result);
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    public void getStudentById_InvalidId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            studentService.getStudentById(-1L);
        });
        assertNotNull(exception);
    }

    @Test
    public void getStudentById_ValidId_ReturnsStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Optional<Student> result = studentService.getStudentById(1L);

        assertTrue(result.isPresent());
        assertEquals(student, result.get());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    public void saveStudent_NullStudent_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            studentService.saveStudent(null);
        });
        assertNotNull(exception);
    }

    @Test
    public void saveStudent_ValidStudent_SavesStudent() {
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.saveStudent(student);

        assertEquals(student, result);
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    public void updateStudent_InvalidId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            studentService.updateStudent(-1L, student);
        });
        assertNotNull(exception);
    }

    @Test
    public void updateStudent_NullStudentDetails_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            studentService.updateStudent(1L, null);
        });
        assertNotNull(exception);
    }

    @Test
    public void updateStudent_ValidIdAndDetails_UpdatesStudent() {
        Student updatedStudent = new Student();
        updatedStudent.setId(1L);
        updatedStudent.setAge(21);
        updatedStudent.setGender("Female");
        updatedStudent.setMajor("Mathematics");
        updatedStudent.setGrade("Junior");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

        Student result = studentService.updateStudent(1L, updatedStudent);

        assertEquals(updatedStudent, result);
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    public void deleteStudent_InvalidId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            studentService.deleteStudent(-1L);
        });
        assertNotNull(exception);
    }

    @Test
    public void deleteStudent_ValidId_DeletesStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.deleteStudent(1L);

        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).deleteById(1L);
    }

    @Test
    public void findStudentById_NonExistentId_ThrowsException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class, () -> {
            studentService.findStudentById(1L);
        });
        assertNotNull(exception);
    }
}
