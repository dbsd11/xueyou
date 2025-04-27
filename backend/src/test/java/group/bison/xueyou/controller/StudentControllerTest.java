package group.bison.xueyou.controller;

import group.bison.xueyou.entity.Student;
import group.bison.xueyou.service.StudentService;
import org.junit.jupiter.api.BeforeEach; // 使用 JUnit 5 的 @BeforeEach
import org.junit.jupiter.api.Test; // 使用 JUnit 5 的 @Test
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // 使用 Mockito 的 JUnit 5 扩展
public class StudentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach // 替换为 JUnit 5 的 @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // 替换为 openMocks 方法
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();
    }

    @Test
    public void getAllStudents_StudentsExist_Returns200() throws Exception {
        Student student = new Student();
        when(studentService.getAllStudents()).thenReturn(Arrays.asList(student));

        mockMvc.perform(get("/api/students")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());

        verify(studentService, times(1)).getAllStudents();
    }

    // 其他测试方法保持不变...
}
