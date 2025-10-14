package com.example.afterproject.repository;

import com.example.afterproject.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    List<AttendanceEntity> findByClassDateAndEnrollment_Course_CourseId(LocalDate classDate, Long courseId);

    Optional<AttendanceEntity> findByEnrollment_EnrollmentIdAndClassDate(Long enrollmentId, LocalDate classDate);

    // [추가] 특정 수강 정보에 대한 모든 출결 상태 조회
    @Query("SELECT a.status FROM AttendanceEntity a WHERE a.enrollment.enrollmentId = :enrollmentId")
    List<String> findStatusByEnrollmentId(@Param("enrollmentId") Long enrollmentId);
}
