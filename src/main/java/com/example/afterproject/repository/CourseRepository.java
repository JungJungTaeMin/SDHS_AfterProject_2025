package com.example.afterproject.repository;

import com.example.afterproject.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByTeacher_UserId(Long teacherId);

    Optional<CourseEntity> findByCourseIdAndTeacher_UserId(Long courseId, Long teacherId);

    List<CourseEntity> findByStatus(String status);

    // ▼ [수정됨] 승인 상태이면서(APPROVED) + 종료 날짜가 오늘 이후인(>=) 강좌만 조회
    @Query("SELECT c FROM CourseEntity c WHERE c.status = 'APPROVED' " +
            "AND c.endDate >= CURRENT_DATE " + // [핵심] 날짜 지난 건 조회 안 됨!
            "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacher.name LIKE %:keyword%) " +
            "AND (:category IS NULL OR c.category = :category)")
    List<CourseEntity> searchApprovedCourses(@Param("keyword") String keyword, @Param("category") String category);

    List<CourseEntity> findByLocationAndStatusNot(String location, String status);

    // 자동 종료 스케줄러용
    List<CourseEntity> findByStatusAndEndDateBefore(String status, LocalDate date);
}