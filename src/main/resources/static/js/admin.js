// ===============================
// 페이지 접근 권한 확인
// ===============================
// 관리자만 접근 가능
// checkAuth('관리자');

// ===============================
// 전역 상태
// ===============================
const currentUser = getCurrentUser();

// ===============================
// API 호출 함수들
// ===============================

// 사용자 목록 조회
async function loadUsers(role = '', name = '') {
  try {
    let endpoint = '/api/admin/users';
    const params = new URLSearchParams();
    if (role) params.append('role', role);
    if (name) params.append('name', name);
    if (params.toString()) endpoint += `?${params.toString()}`;

    const data = await apiRequest(endpoint);
    return data;
  } catch (error) {
    console.error('Failed to load users:', error);
    alert('사용자 목록을 불러오는데 실패했습니다.');
    return [];
  }
}

// 사용자 역할 변경
async function changeUserRole(userId, role) {
  try {
    await apiRequest(`/api/admin/users/${userId}/role`, {
      method: 'PUT',
      body: JSON.stringify({ role })
    });
    return true;
  } catch (error) {
    console.error('Failed to change user role:', error);
    throw error;
  }
}

// 사용자 삭제
async function deleteUser(userId) {
  try {
    await apiRequest(`/api/admin/users/${userId}`, {
      method: 'DELETE'
    });
    return true;
  } catch (error) {
    console.error('Failed to delete user:', error);
    throw error;
  }
}

// 승인 대기 강좌 목록
async function loadPendingCourses() {
  try {
    const data = await apiRequest('/api/admin/courses/pending');
    return data;
  } catch (error) {
    console.error('Failed to load pending courses:', error);
    return [];
  }
}

// 강좌 승인/반려
async function changeCourseStatus(courseId, status) {
  try {
    await apiRequest(`/api/admin/courses/${courseId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    });
    return true;
  } catch (error) {
    console.error('Failed to change course status:', error);
    throw error;
  }
}

// 전체 강좌 목록
async function loadAllCourses() {
  try {
    const data = await apiRequest('/api/admin/courses');
    return data;
  } catch (error) {
    console.error('Failed to load all courses:', error);
    return [];
  }
}

// 강제 배정
async function forceEnroll(courseId, studentId) {
  try {
    await apiRequest(`/api/admin/courses/${courseId}/enroll`, {
      method: 'POST',
      body: JSON.stringify({ studentId })
    });
    return true;
  } catch (error) {
    console.error('Failed to force enroll:', error);
    throw error;
  }
}

// 강제 취소
async function forceUnenroll(courseId, studentId) {
  try {
    await apiRequest(`/api/admin/courses/${courseId}/unenroll/${studentId}`, {
      method: 'DELETE'
    });
    return true;
  } catch (error) {
    console.error('Failed to force unenroll:', error);
    throw error;
  }
}

// 전체 공지 작성
async function createGlobalNotice(noticeData) {
  try {
    await apiRequest('/api/admin/notices', {
      method: 'POST',
      body: JSON.stringify(noticeData)
    });
    return true;
  } catch (error) {
    console.error('Failed to create notice:', error);
    throw error;
  }
}

// 전체 설문 생성
async function createGlobalSurvey(surveyData) {
  try {
    await apiRequest('/api/admin/surveys', {
      method: 'POST',
      body: JSON.stringify(surveyData)
    });
    return true;
  } catch (error) {
    console.error('Failed to create survey:', error);
    throw error;
  }
}

// ===============================
// UI 렌더링
// ===============================

// 로그아웃
document.getElementById('btnLogout').addEventListener('click', logout);

// 메뉴 전환
document.querySelectorAll('.menu-item').forEach(btn => {
  btn.addEventListener('click', e => {
    e.preventDefault();
    document.querySelectorAll('.menu-item').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    const target = btn.dataset.target;
    document.querySelectorAll('.page-section').forEach(sec => sec.classList.add('hidden'));
    document.getElementById(target).classList.remove('hidden');

    // 페이지 진입 시 데이터 로드
    if (target === 'users') renderUsers();
    if (target === 'courses') {
      renderPendingCourses();
      renderAllCourses();
    }
  });
});

// 탭 전환
document.querySelectorAll('.tabs .tab').forEach(tab => {
  tab.addEventListener('click', () => {
    const parentTabs = tab.closest('.tabs');
    parentTabs.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');

    const section = tab.closest('.page-section');
    section.querySelectorAll('.tab-panel').forEach(p => p.classList.add('hidden'));
    section.querySelector(`#tab_${tab.dataset.tab}`).classList.remove('hidden');
  });
});

// ===============================
// 1. 사용자 관리
// ===============================
async function renderUsers() {
  const tbody = document.getElementById('userTbody');
  tbody.innerHTML = '<tr><td colspan="5">로딩 중...</td></tr>';

  const keyword = document.getElementById('userSearch').value.trim();
  const roleFilter = document.getElementById('roleFilter').value;

  const users = await loadUsers(roleFilter, keyword);

  tbody.innerHTML = '';

  users.forEach(u => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${u.name}</td>
      <td>${u.email}</td>
      <td>
        <select class="roleSelect" data-id="${u.userId}">
          <option ${u.role === 'STUDENT' || u.role === '학생' ? 'selected' : ''}>학생</option>
          <option ${u.role === 'TEACHER' || u.role === '교사' ? 'selected' : ''}>교사</option>
          <option ${u.role === 'ADMIN' || u.role === '관리자' ? 'selected' : ''}>관리자</option>
        </select>
      </td>
      <td><span class="badge green">활성</span></td>
      <td>
        <button class="ghost btnDelete" data-id="${u.userId}">삭제</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

document.getElementById('userSearch').addEventListener('input', renderUsers);
document.getElementById('roleFilter').addEventListener('change', renderUsers);

document.getElementById('userTbody').addEventListener('change', async e => {
  if (e.target.classList.contains('roleSelect')) {
    const userId = e.target.dataset.id;
    const newRole = e.target.value;

    try {
      await changeUserRole(userId, newRole);
      alert('역할이 변경되었습니다.');
    } catch (error) {
      alert(error.message || '역할 변경에 실패했습니다.');
      renderUsers();
    }
  }
});

document.getElementById('userTbody').addEventListener('click', async e => {
  if (e.target.classList.contains('btnDelete')) {
    const userId = e.target.dataset.id;
    if (confirm('정말로 삭제하시겠습니까?')) {
      try {
        await deleteUser(userId);
        alert('사용자가 삭제되었습니다.');
        renderUsers();
      } catch (error) {
        alert(error.message || '삭제에 실패했습니다.');
      }
    }
  }
});

document.getElementById('btnAddUser').addEventListener('click', () => {
  alert('신규 사용자 추가 기능은 회원가입 API가 필요합니다.');
});

// ===============================
// 2. 강좌 운영 관리
// ===============================
async function renderPendingCourses() {
  const tbody = document.getElementById('pendingTbody');
  tbody.innerHTML = '<tr><td colspan="5">로딩 중...</td></tr>';

  const courses = await loadPendingCourses();

  tbody.innerHTML = '';

  courses.forEach(c => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${c.courseName}</td>
      <td>${c.teacherName || '-'}</td>
      <td>${c.createdAt || '-'}</td>
      <td><span class="badge yellow">대기</span></td>
      <td>
        <button class="primary btnApprove" data-id="${c.courseId}">승인</button>
        <button class="ghost btnReject" data-id="${c.courseId}">반려</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

document.getElementById('pendingTbody').addEventListener('click', async e => {
  const courseId = e.target.dataset.id;
  if (!courseId) return;

  try {
    if (e.target.classList.contains('btnApprove')) {
      await changeCourseStatus(courseId, 'APPROVED');
      alert('강좌가 승인되었습니다.');
    }
    if (e.target.classList.contains('btnReject')) {
      await changeCourseStatus(courseId, 'REJECTED');
      alert('강좌가 반려되었습니다.');
    }
    renderPendingCourses();
    renderAllCourses();
  } catch (error) {
    alert(error.message || '처리에 실패했습니다.');
  }
});

async function renderAllCourses() {
  const tbody = document.getElementById('allCourseTbody');
  tbody.innerHTML = '<tr><td colspan="5">로딩 중...</td></tr>';

  const courses = await loadAllCourses();
  const keyword = document.getElementById('courseSearch').value.trim().toLowerCase();
  const statusFilter = document.getElementById('statusFilter').value;

  let filtered = courses;
  if (keyword) {
    filtered = filtered.filter(c =>
      c.courseName.toLowerCase().includes(keyword) ||
      (c.teacherName && c.teacherName.toLowerCase().includes(keyword))
    );
  }
  if (statusFilter) {
    filtered = filtered.filter(c => c.status.toLowerCase() === statusFilter);
  }

  tbody.innerHTML = '';

  filtered.forEach(c => {
    let color = 'yellow';
    let statusText = '대기';
    if (c.status === 'APPROVED' || c.status === 'approved') {
      color = 'green';
      statusText = '승인';
    }
    if (c.status === 'REJECTED' || c.status === 'rejected') {
      color = 'red';
      statusText = '반려';
    }

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${c.courseName}</td>
      <td>${c.teacherName || '-'}</td>
      <td>${c.capacity || 0}</td>
      <td>${c.currentEnrollmentCount || 0}/${c.capacity || 0}</td>
      <td><span class="badge ${color}">${statusText}</span></td>
    `;
    tbody.appendChild(tr);
  });
}

document.getElementById('courseSearch').addEventListener('input', renderAllCourses);
document.getElementById('statusFilter').addEventListener('change', renderAllCourses);

// ===============================
// 3. 공지 및 설문 관리
// ===============================
document.getElementById('btnNoticeSave').addEventListener('click', async () => {
  const title = document.getElementById('noticeTitle').value.trim();
  const content = document.getElementById('noticeBody').value.trim();

  if (!title || !content) {
    alert('제목과 내용을 입력하세요.');
    return;
  }

  try {
    await createGlobalNotice({ title, content });
    alert('공지사항이 등록되었습니다.');
    document.getElementById('noticeTitle').value = '';
    document.getElementById('noticeBody').value = '';
  } catch (error) {
    alert(error.message || '공지 등록에 실패했습니다.');
  }
});

document.getElementById('btnNoticeReset').addEventListener('click', () => {
  document.getElementById('noticeTitle').value = '';
  document.getElementById('noticeBody').value = '';
});

document.getElementById('btnSurveySave').addEventListener('click', () => {
  alert('설문조사 생성 기능은 준비 중입니다.');
  // TODO: 설문 생성 API 연동
});

document.getElementById('btnSurveyReset').addEventListener('click', () => {
  document.getElementById('surveyTitle').value = '';
  document.getElementById('surveyPeriod').value = '';
  document.getElementById('questionWrap').innerHTML = '';
});

// ===============================
// 초기 렌더링
// ===============================
renderUsers();