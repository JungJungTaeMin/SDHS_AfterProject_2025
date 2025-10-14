package com.example.afterproject.repository;

import com.example.afterproject.entity.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {
    // 특정 강좌의 모든 설문 조회
    List<SurveyEntity> findByCourse_CourseId(Long courseId);

    List<SurveyEntity> findByCourse_CourseIdIn(List<Long> courseIds);

    // [추가] 전체 설문 목록 조회 (관리자용, course가 null인 경우)
    List<SurveyEntity> findByCourseIsNull();
}
