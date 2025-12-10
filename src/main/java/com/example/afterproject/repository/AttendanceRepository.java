package com.example.afterproject.repository;

import com.example.afterproject.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    List<AttendanceEntity> findByClassDateAndEnrollment_Course_CourseId(LocalDate classDate, Long courseId);

    Optional<AttendanceEntity> findByEnrollment_EnrollmentIdAndClassDate(Long enrollmentId, LocalDate classDate);

    // [기존] 상태만 조회 (삭제하거나 유지해도 됨)
    // @Query("SELECT a.status FROM AttendanceEntity a WHERE a.enrollment.enrollmentId = :enrollmentId")
    // List<String> findStatusByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

    // [추가] 상세 기록 조회 (날짜 내림차순)
    List<AttendanceEntity> findByEnrollment_EnrollmentIdOrderByClassDateDesc(Long enrollmentId);
}