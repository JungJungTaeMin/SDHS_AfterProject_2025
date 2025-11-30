package com.example.afterproject.repository;

import com.example.afterproject.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByTeacher_UserId(Long teacherId);

    Optional<CourseEntity> findByCourseIdAndTeacher_UserId(Long courseId, Long teacherId);

    List<CourseEntity> findByStatus(String status);

    /**
     * 학생용 강좌 검색 (승인된 강좌만)
     * @param keyword 강좌명 또는 강사명
     * @param category 카테고리
     * @return 검색된 강좌 목록
     */
    @Query("SELECT c FROM CourseEntity c WHERE c.status = 'APPROVED' AND " +
            "(:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacher.name LIKE %:keyword%) AND " +
            "(:category IS NULL OR c.category = :category)")
    List<CourseEntity> searchApprovedCourses(@Param("keyword") String keyword, @Param("category") String category);
}

