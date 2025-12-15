package com.example.afterproject.service;

import com.example.afterproject.dto.*;
import com.example.afterproject.entity.*;
import com.example.afterproject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled; // [필수] 스케줄러
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherCourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final SurveyRepository surveyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NoticeRepository noticeRepository;
    private final AttendanceRepository attendanceRepository;

    private static final List<String> ALLOWED_ROOMS = Arrays.asList(
            "206", "207", "301", "302", "305", "306", "307", "308", "309", "강당", "406"
    );

    // 1.1. 강좌 개설 (분기, 종료일 추가됨)
    @Transactional
    public CourseDto createCourse(Long teacherId, CourseCreateDto createDto) {
        UserEntity teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        String cleanLocation = createDto.getLocation().trim();

        if (!ALLOWED_ROOMS.contains(cleanLocation)) {
            throw new IllegalArgumentException("허용되지 않은 강의실입니다. (" + cleanLocation + ")");
        }

        List<CourseEntity> existingCourses = courseRepository.findByLocationAndStatusNot(cleanLocation, "REJECTED");
        for (CourseEntity existing : existingCourses) {
            // [추가 검사] 종료된 강좌(CLOSED)는 중복 체크에서 제외 (강의실 재사용 가능하게)
            if ("CLOSED".equals(existing.getStatus())) continue;

            if (isTimeOverlap(existing.getCourseTime(), createDto.getCourseTime()) &&
                    isDayOverlap(existing.getCourseDays(), createDto.getCourseDays())) {
                throw new IllegalStateException("해당 강의실은 이미 예약되어 있습니다: " + existing.getCourseName());
            }
        }

        CourseEntity course = CourseEntity.builder()
                .teacher(teacher)
                .courseName(createDto.getCourseName())
                .category(createDto.getCategory())
                .description(createDto.getDescription())
                .courseDays(createDto.getCourseDays())
                .courseTime(createDto.getCourseTime())
                .location(cleanLocation)
                .capacity(createDto.getCapacity())
                .status("PENDING")
                .quarter(createDto.getQuarter()) // [추가]
                .endDate(createDto.getEndDate()) // [추가]
                .build();

        CourseEntity savedCourse = courseRepository.save(course);
        return new CourseDto(savedCourse);
    }

    // [기존 메서드 유지]
    private boolean isTimeOverlap(String time1, String time2) {
        return time1.equals(time2);
    }
    private boolean isDayOverlap(String days1, String days2) {
        if (days1 == null || days2 == null) return false;
        List<String> d1 = Arrays.asList(days1.split(","));
        List<String> d2 = Arrays.asList(days2.split(","));
        return !Collections.disjoint(d1, d2);
    }

    // [기존] 내 강좌 조회
    @Transactional(readOnly = true)
    public List<CourseDto> getMyCourses(Long teacherId) {
        List<CourseEntity> courses = courseRepository.findByTeacher_UserId(teacherId);
        return courses.stream()
                .map(course -> {
                    CourseDto dto = new CourseDto(course);
                    long count = enrollmentRepository.countByCourse_CourseIdAndStatus(course.getCourseId(), "ACTIVE");
                    dto.setCurrentEnrollmentCount(count);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ... (나머지 기존 update, delete 등의 메서드들은 그대로 두셔도 됩니다) ...

    // ============================================================
    // ▼ [핵심 기능] 매일 자정(00:00:00)에 종료된 강좌 자동 마감
    // ============================================================
    @Scheduled(cron = "0 0 0 * * *")
    public void autoCloseExpiredCourses() {
        LocalDate today = LocalDate.now();

        // 상태가 APPROVED이고, 종료 날짜가 오늘보다 이전인(어제까지인) 강좌 찾기
        List<CourseEntity> expiredCourses = courseRepository.findByStatusAndEndDateBefore("APPROVED", today);

        for (CourseEntity course : expiredCourses) {
            course.setStatus("CLOSED"); // 상태를 'CLOSED'로 변경
        }

        // 변경사항 저장 (JPA Dirty Checking으로 자동 저장되지만 명시적으로)
        courseRepository.saveAll(expiredCourses);

        if (!expiredCourses.isEmpty()) {
            System.out.println("📅 [스케줄러] 기간 만료된 강좌 " + expiredCourses.size() + "건을 종료 처리했습니다.");
        }
    }

    // (기존 나머지 메서드들 - getEnrolledStudents, recordAttendance 등은 아래에 그대로 유지...)
    // 코드가 너무 길어 생략하지만, 기존 코드를 지우지 말고 이 부분만 사이에 끼워넣으시면 됩니다.
    // 하지만 가장 확실한 건, 기존 TeacherCourseService.java 파일에
    // 위 createCourse 메서드를 덮어쓰고, 맨 아래에 autoCloseExpiredCourses를 추가하는 것입니다.
}