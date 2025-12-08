// ===============================
// 페이지 접근 권한 확인
// ===============================
// 교사만 접근 가능
// checkAuth('교사');

// ===============================
// 전역 상태
// ===============================
const currentUser = getCurrentUser();
let MY_COURSES = []; // 내 담당 강좌 목록

// ===============================
// API 호출 함수들
// ===============================

// 강좌 개설 신청
async function createCourse(courseData) {
  try {
    const response = await apiRequest('/api/teachers/courses', {
      method: 'POST',
      body: JSON.stringify(courseData)
    });
    return response;
  } catch (error) {
    console.error('Failed to create course:', error);
    throw error;
  }
}

// 내 담당 강좌 목록 조회
async function loadMyCourses() {
  try {
    const data = await apiRequest('/api/teachers/courses/my');
    MY_COURSES = data;
    return MY_COURSES;
  } catch (error) {
    console.error('Failed to load my courses:', error);
    alert('강좌 목록을 불러오는데 실패했습니다.');
    return [];
  }
}

// 강좌 정보 수정
async function updateCourse(courseId, courseData) {
  try {
    const response = await apiRequest(`/api/teachers/courses/${courseId}`, {
      method: 'PUT',
      body: JSON.stringify(courseData)
    });
    return response;
  } catch (error) {
    console.error('Failed to update course:', error);
    throw error;
  }
}

// 수강생 목록 조회
async function getStudentList(courseId) {
  try {
    const data = await apiRequest(`/api/teachers/courses/${courseId}/students`);
    return data;
  } catch (error) {
    console.error('Failed to load students:', error);
    return [];
  }
}

// 출결 현황 조회
async function getAttendance(courseId, classDate) {
  try {
    const data = await apiRequest(`/api/teachers/courses/${courseId}/attendance?classDate=${classDate}`);
    return data;
  } catch (error) {
    console.error('Failed to load attendance:', error);
    return [];
  }
}

// 출결 기록/수정
async function saveAttendance(courseId, attendanceData) {
  try {
    await apiRequest(`/api/teachers/courses/${courseId}/attendance`, {
      method: 'POST',
      body: JSON.stringify(attendanceData)
    });
    return true;
  } catch (error) {
    console.error('Failed to save attendance:', error);
    throw error;
  }
}

// 공지사항 목록 조회
async function getNotices(courseId) {
  try {
    const data = await apiRequest(`/api/teachers/courses/${courseId}/notices`);
    return data;
  } catch (error) {
    console.error('Failed to load notices:', error);
    return [];
  }
}

// 공지사항 작성
async function createNotice(courseId, noticeData) {
  try {
    const response = await apiRequest(`/api/teachers/courses/${courseId}/notices`, {
      method: 'POST',
      body: JSON.stringify(noticeData)
    });
    return response;
  } catch (error) {
    console.error('Failed to create notice:', error);
    throw error;
  }
}

// 공지사항 수정
async function updateNotice(courseId, noticeId, noticeData) {
  try {
    const response = await apiRequest(`/api/teachers/courses/${courseId}/notices/${noticeId}`, {
      method: 'PUT',
      body: JSON.stringify(noticeData)
    });
    return response;
  } catch (error) {
    console.error('Failed to update notice:', error);
    throw error;
  }
}

// 공지사항 삭제
async function deleteNotice(courseId, noticeId) {
  try {
    await apiRequest(`/api/teachers/courses/${courseId}/notices/${noticeId}`, {
      method: 'DELETE'
    });
    return true;
  } catch (error) {
    console.error('Failed to delete notice:', error);
    throw error;
  }
}

// 설문조사 목록 조회
async function getSurveys(courseId) {
  try {
    const data = await apiRequest(`/api/teachers/courses/${courseId}/surveys`);
    return data;
  } catch (error) {
    console.error('Failed to load surveys:', error);
    return [];
  }
}

// 설문조사 생성
async function createSurvey(courseId, surveyData) {
  try {
    const response = await apiRequest(`/api/teachers/courses/${courseId}/surveys`, {
      method: 'POST',
      body: JSON.stringify(surveyData)
    });
    return response;
  } catch (error) {
    console.error('Failed to create survey:', error);
    throw error;
  }
}

// ===============================
// UI 렌더링
// ===============================

// 헤더 교사 정보 표시
document.getElementById('teacherName').textContent = currentUser.name || '교사';
document.getElementById('teacherEmail').textContent = currentUser.email || '';
document.getElementById('teacherAvatar').textContent = (currentUser.name || '교').slice(0, 1);

// 로그아웃
document.getElementById('btnLogout').addEventListener('click', logout);

// 강좌 목록 렌더링
async function renderCourseList() {
  const box = document.getElementById('courseList');

  await loadMyCourses();

  if (MY_COURSES.length === 0) {
    box.innerHTML = `<div class="tip">아직 강좌가 없어요. '신규 강좌 개설 신청'을 눌러 시작해요.</div>`;
    return;
  }

  box.innerHTML = MY_COURSES.map(c => {
    const statusText = {
      'PENDING': '대기',
      'APPROVED': '승인',
      'REJECTED': '반려',
      'pending': '대기',
      'approved': '승인',
      'rejected': '반려'
    }[c.status] || c.status;

    return `
      <div class="course-card" data-id="${c.courseId}">
        <div class="course-title">
          <h3>${c.courseName}</h3>
          <span class="status ${c.status.toLowerCase()}">${statusText}</span>
        </div>
        <div class="meta">
          <div><b>정원</b> ${c.currentEnrollmentCount || 0}/${c.capacity || 0}</div>
        </div>
        <div class="card-actions">
          ${c.status === 'APPROVED' || c.status === 'approved' ? `<button class="primary" data-manage="${c.courseId}">관리</button>` : ''}
          ${c.status === 'PENDING' || c.status === 'REJECTED' || c.status === 'pending' || c.status === 'rejected' ? `<button class="ghost" data-edit="${c.courseId}">수정</button>` : ''}
        </div>
      </div>
    `;
  }).join('');

  // 관리 버튼 핸들러
  document.querySelectorAll('#courseList [data-manage]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-manage');
      const course = MY_COURSES.find(c => c.courseId == id);
      alert(`강좌 관리 기능은 준비 중입니다: ${course.courseName}`);
      // TODO: 강좌 상세 관리 모달 열기
    });
  });

  // 수정 버튼 핸들러
  document.querySelectorAll('#courseList [data-edit]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-edit');
      const course = MY_COURSES.find(c => c.courseId == id);
      alert(`강좌 수정 기능은 준비 중입니다: ${course.courseName}`);
      // TODO: 강좌 수정 모달 열기
    });
  });
}

// 신규 강좌 개설 버튼
document.getElementById('btnNewCourse').addEventListener('click', async () => {
  const courseName = prompt('강좌명을 입력하세요:');
  if (!courseName) return;

  const category = prompt('카테고리를 입력하세요:');
  if (!category) return;

  const courseDays = prompt('수업 요일을 입력하세요 (예: 월,수):');
  if (!courseDays) return;

  const courseTime = prompt('수업 시간을 입력하세요 (예: 16:00-18:00):');
  if (!courseTime) return;

  const capacity = parseInt(prompt('정원을 입력하세요:'), 10);
  if (!capacity || capacity < 1) {
    alert('정원은 1명 이상이어야 합니다.');
    return;
  }

  const description = prompt('강좌 설명을 입력하세요:');

  try {
    await createCourse({
      courseName,
      category,
      description,
      courseDays,
      courseTime,
      location: '',
      capacity
    });

    alert('강좌 개설 신청이 완료되었습니다. 관리자 승인을 기다려주세요.');
    await renderCourseList();
  } catch (error) {
    alert(error.message || '강좌 개설 신청에 실패했습니다.');
  }
});

// 초기화
renderCourseList();