package com.example.afterproject.repository;

import com.example.afterproject.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 추가
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByTeacher_UserId(Long teacherId);

    Optional<CourseEntity> findByCourseIdAndTeacher_UserId(Long courseId, Long teacherId);

    List<CourseEntity> findByStatus(String status);

    @Query("SELECT c FROM CourseEntity c WHERE c.status = 'APPROVED' AND " +
            "(:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacher.name LIKE %:keyword%) AND " +
            "(:category IS NULL OR c.category = :category)")
    List<CourseEntity> searchApprovedCourses(@Param("keyword") String keyword, @Param("category") String category);

    // [추가] 특정 강의실을 사용 중인 강좌 조회 (반려된 강좌는 제외)
    List<CourseEntity> findByLocationAndStatusNot(String location, String status);
}