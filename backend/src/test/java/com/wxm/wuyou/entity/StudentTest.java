package com.wxm.wuyou.entity;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentTest {

    private Student student;

    @BeforeEach
    public void setUp() {
        student = new Student();
    }

    @Test
    public void setGender_ValidGender_ShouldSetCorrectly() {
        student.setGender("Male");
        assertEquals("male", student.getGender());

        student.setGender("FEMALE");
        assertEquals("female", student.getGender());
    }

    @Test
    public void setGender_InvalidGender_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            student.setGender("other");
        });
    }

    @Test
    public void setMajor_ValidMajor_ShouldSetCorrectly() {
        student.setMajor(" Computer Science ");
        assertEquals("Computer Science", student.getMajor());
    }

    @Test
    public void setMajor_NullOrEmptyMajor_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            student.setMajor(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            student.setMajor("   ");
        });
    }

    @Test
    public void setGrade_ValidGrade_ShouldSetCorrectly() {
        student.setGrade(" Freshman ");
        assertEquals("Freshman", student.getGrade());
    }

    @Test
    public void setGrade_NullOrEmptyGrade_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            student.setGrade(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            student.setGrade("   ");
        });
    }
}
