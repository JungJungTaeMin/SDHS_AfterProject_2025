package com.example.afterproject.repository;

import com.example.afterproject.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    /**
     * 특정 강좌에 게시된 모든 공지사항을 조회합니다.
     */
    List<NoticeEntity> findByCourse_CourseId(Long courseId);

    /**
     * 특정 강좌에 속한 특정 공지사항을 조회합니다. (수정/삭제 시 권한 확인용)
     */
    Optional<NoticeEntity> findByNoticeIdAndCourse_CourseId(Long noticeId, Long courseId);

    // [추가] 전체 공지사항 목록 조회 (관리자용, course가 null인 경우)
    List<NoticeEntity> findByCourseIsNull();
}
