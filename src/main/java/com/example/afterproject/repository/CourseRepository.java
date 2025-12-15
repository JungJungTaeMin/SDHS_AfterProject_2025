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

    // 특정 선생님의 강좌 목록
    List<CourseEntity> findByTeacher_UserId(Long teacherId);

    // 특정 선생님의 특정 강좌 상세 (소유권 확인용)
    Optional<CourseEntity> findByCourseIdAndTeacher_UserId(Long courseId, Long teacherId);

    // 상태별 조회 (PENDING, APPROVED 등)
    List<CourseEntity> findByStatus(String status);

    // ▼ [수정됨] 학생용 강좌 검색 (핵심 수정 부분)
    // 조건: 1. 승인됨(APPROVED)
    //       2. 종료일이 오늘보다 미래이거나(>=) OR 종료일이 아예 없거나(NULL) -> 기존 데이터 표시용
    //       3. 검색어 및 카테고리 필터링
    @Query("SELECT c FROM CourseEntity c WHERE c.status = 'APPROVED' " +
            "AND (c.endDate >= CURRENT_DATE OR c.endDate IS NULL) " + // [핵심] NULL 허용 추가!
            "AND (:keyword IS NULL OR c.courseName LIKE %:keyword% OR c.teacher.name LIKE %:keyword%) " +
            "AND (:category IS NULL OR c.category = :category)")
    List<CourseEntity> searchApprovedCourses(@Param("keyword") String keyword, @Param("category") String category);

    // 강의실 중복 예약 방지용 (특정 강의실, 특정 상태 제외)
    List<CourseEntity> findByLocationAndStatusNot(String location, String status);

    // [스케줄러용] 상태가 승인됨(APPROVED)이면서, 종료 날짜가 특정 날짜(어제) 이전인 강좌 찾기
    List<CourseEntity> findByStatusAndEndDateBefore(String status, LocalDate date);
}