package com.example.afterproject.dto;

import com.example.afterproject.entity.UserEntity;
import lombok.Getter;

/**
 * 강좌에 등록된 학생 정보를 전달하기 위한 DTO 클래스입니다.
 */
@Getter
public class EnrolledStudentDto {

    private final String studentIdNo; // 학생의 학번
    private final String name;        // 학생의 이름

    /**
     * UserEntity 객체를 받아 DTO를 생성합니다.
     * @param student 학생 정보를 담고 있는 UserEntity 객체
     */
    public EnrolledStudentDto(UserEntity student) {
        this.studentIdNo = student.getStudentIdNo();
        this.name = student.getName();
    }
}
