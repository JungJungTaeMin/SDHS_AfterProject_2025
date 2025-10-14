package com.example.afterproject.dto;

import com.example.afterproject.entity.CourseEntity;
import com.example.afterproject.entity.UserEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseCreateDto {

    @NotBlank(message = "강좌명은 필수 입력 항목입니다.")
    private String courseName;

    private String category;

    private String description;

    @NotBlank(message = "수업 요일은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[월화수목금토일](,[월화수목금토일])*$", message = "요일은 '화,목'과 같이 쉼표로 구분된 형식이어야 합니다.")
    private String courseDays;

    @NotBlank(message = "수업 시간은 필수 입력 항목입니다.")
    private String courseTime;

    private String location;

    @NotNull(message = "정원은 필수 입력 항목입니다.")
    @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
    private int capacity;

    public CourseEntity toEntity(UserEntity teacher) {
        return CourseEntity.builder()
                .teacher(teacher)
                .courseName(this.courseName)
                .category(this.category)
                .description(this.description)
                .courseDays(this.courseDays)
                .courseTime(this.courseTime)
                .location(this.location)
                .capacity(this.capacity)
                .status("PENDING")
                .build();
    }
}
