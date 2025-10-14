// EnrollmentRepository.java

package com.example.afterproject.repository;

import com.example.afterproject.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    List<EnrollmentEntity> findByCourse_CourseId(Long courseId);

    Optional<EnrollmentEntity> findByStudent_UserIdAndCourse_CourseId(Long studentId, Long courseId);

    long countByCourse_CourseIdAndStatus(Long courseId, String status);

    @Query("SELECT e.course.courseId FROM EnrollmentEntity e WHERE e.student.userId = :studentId AND e.status = 'ACTIVE'")
    List<Long> findActiveCourseIdsByStudent_UserId(@Param("studentId") Long studentId); // ✅ 이 메소드를 사용합니다.

    List<EnrollmentEntity> findByStudent_UserId(Long studentId);

    List<EnrollmentEntity> findByCourse_CourseIdAndStatus(Long courseId, String status);

    // List<Long> findActiveCourseIdsByStudentId(Long studentId); // ❌ 이 라인을 삭제하세요.
}