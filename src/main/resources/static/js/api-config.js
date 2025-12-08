/* =====================================
   API Configuration & Utilities
   Railway 백엔드 서버와 통신하기 위한 설정
===================================== */

// Railway 백엔드 서버 주소
const API_BASE_URL = 'https://sdhsafterproject2025-production.up.railway.app';

// LocalStorage 키
const AUTH_TOKEN_KEY = 'afterschool.authToken';
const CURRENT_USER_KEY = 'afterschool.currentUser';

/* -------------------------------------
   인증 토큰 관리
------------------------------------- */
function getAuthToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

function setAuthToken(token) {
  localStorage.setItem(AUTH_TOKEN_KEY, token);
}

function clearAuthToken() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(CURRENT_USER_KEY);
}

function getCurrentUser() {
  const role = localStorage.getItem(CURRENT_USER_KEY);
  return role ? { role } : null;
}

function setCurrentUser(role) {
  localStorage.setItem(CURRENT_USER_KEY, role);
}

/* -------------------------------------
   공통 API 요청 함수
------------------------------------- */
async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = getAuthToken();

  // 기본 헤더 설정
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // 인증 토큰이 있으면 헤더에 추가
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, config);

    // 401 Unauthorized - 토큰 만료 또는 인증 실패
    if (response.status === 401) {
      clearAuthToken();
      alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
      window.location.href = './login.html';
      throw new Error('Unauthorized');
    }

    // 403 Forbidden - 권한 없음
    if (response.status === 403) {
      alert('접근 권한이 없습니다.');
      throw new Error('Forbidden');
    }

    // 404 Not Found
    if (response.status === 404) {
      throw new Error('요청한 리소스를 찾을 수 없습니다.');
    }

    // 500 Server Error
    if (response.status >= 500) {
      throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }

    // 응답이 JSON인지 확인
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || '요청 처리 중 오류가 발생했습니다.');
      }

      return data;
    }

    // JSON이 아닌 경우 (204 No Content 등)
    if (response.ok) {
      return null;
    }

    throw new Error('요청 처리 중 오류가 발생했습니다.');

  } catch (error) {
    // 네트워크 오류 (CORS, 서버 다운 등)
    if (error.message === 'Failed to fetch' || error.name === 'TypeError') {
      console.error('Network error:', error);
      throw new Error('서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.');
    }

    throw error;
  }
}

/* -------------------------------------
   페이지 접근 권한 확인
------------------------------------- */
function checkAuth(requiredRole = null) {
  const token = getAuthToken();
  const user = getCurrentUser();

  // 토큰이 없으면 로그인 페이지로
  if (!token || !user) {
    window.location.href = './login.html';
    return false;
  }

  // 특정 역할이 필요한 경우 확인
  // API는 'STUDENT', 'TEACHER', 'ADMIN' 반환
  // 프론트엔드 checkAuth('학생') 호출 시 매핑 필요
  const roleMap = {
    '학생': 'STUDENT',
    '교사': 'TEACHER',
    '관리자': 'ADMIN'
  };

  const requiredRoleCode = roleMap[requiredRole] || requiredRole;

  if (requiredRole && user.role !== requiredRoleCode && user.role !== requiredRole) {
    alert(`접근 권한이 없습니다. (필요: ${requiredRole}, 현재: ${user.role})`);
    // 역할에 맞는 페이지로 리다이렉트
    switch (user.role) {
      case 'ADMIN':
      case '관리자':
        window.location.href = './admin.html';
        break;
      case 'TEACHER':
      case '교사':
        window.location.href = './teacher.html';
        break;
      case 'STUDENT':
      case '학생':
        window.location.href = './student.html';
        break;
      default:
        window.location.href = './login.html';
    }
    return false;
  }

  return true;
}

/* -------------------------------------
   로그아웃
------------------------------------- */
function logout() {
  if (confirm('로그아웃 하시겠습니까?')) {
    clearAuthToken();
    window.location.href = './login.html';
  }
}
