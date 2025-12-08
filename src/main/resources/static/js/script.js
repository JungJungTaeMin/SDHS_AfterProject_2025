// ===============================
// 페이지 접근 권한 확인
// ===============================
// 학생만 접근 가능
// checkAuth('학생');

// ===============================
// 데이터 & 상태
// ===============================
const images = {
  code: "https://images.unsplash.com/photo-1518779578993-ec3579fee39f?auto=format&fit=crop&w=1200&q=60",
  write: "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?auto=format&fit=crop&w=1200&q=60",
  art: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=60",
  eng: "https://images.unsplash.com/photo-1529070538774-1843cb3265df?auto=format&fit=crop&w=1200&q=60",
  soccer: "https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=1200&q=60",
  piano: "https://images.unsplash.com/photo-1508780709619-79562169bc64?auto=format&fit=crop&w=1200&q=60",
  cook: "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=60",
  robot: "https://images.unsplash.com/photo-1581090464777-f3220bbe1b8f?auto=format&fit=crop&w=1200&q=60",
  dance: "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=1200&q=60",
  math: "https://images.unsplash.com/photo-1509223197845-458d87318791?auto=format&fit=crop&w=1200&q=60",
};

// 전역 상태 (API에서 로드)
let COURSES = [];
let MY_COURSES = []; // 내 수강 내역
let SURVEYS = [];
let SURVEY_ANS = {};

// ===============================
// API 호출 함수들
// ===============================

// 강좌 목록 조회
async function loadCourses(keyword = '', category = '') {
  try {
    let endpoint = '/api/students/courses';
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (category) params.append('category', category);
    if (params.toString()) endpoint += `?${params.toString()}`;

    const data = await apiRequest(endpoint);

    // API 응답을 기존 형식에 맞게 변환
    COURSES = data.map(course => ({
      id: course.courseId,
      name: course.courseName,
      teacher: course.teacherName,
      category: course.category || '기타',
      tags: [],
      days: course.courseDays ? course.courseDays.split(',') : [],
      time: course.courseTime || '',
      room: course.location || '',
      capacity: course.capacity,
      enrolled: course.currentEnrollment,
      img: images.code, // 기본 이미지
      desc: course.description || '',
      isEnrolled: course.isEnrolled || false // API에서 제공하는 신청 여부
    }));

    return COURSES;
  } catch (error) {
    console.error('Failed to load courses:', error);
    alert('강좌 목록을 불러오는데 실패했습니다.');
    return [];
  }
}

// 강좌 상세 조회
async function getCourseDetail(courseId) {
  try {
    const data = await apiRequest(`/api/students/courses/${courseId}`);

    return {
      id: data.courseId,
      name: data.courseName,
      teacher: data.teacherName,
      category: data.category || '기타',
      tags: [],
      days: data.courseDays ? data.courseDays.split(',') : [],
      time: data.courseTime || '',
      room: data.location || '',
      capacity: data.capacity,
      enrolled: data.currentEnrollment,
      img: images.code,
      desc: data.description || '',
      isEnrolled: data.isEnrolled || false,
      canEnroll: data.canEnroll !== undefined ? data.canEnroll : true // API에서 제공하는 신청 가능 여부
    };
  } catch (error) {
    console.error('Failed to load course detail:', error);
    return null;
  }
}

// 내 수강 내역 조회
async function loadMyCourses() {
  try {
    const data = await apiRequest('/api/students/my-courses');

    // API 응답 형식: { courses: [...], overallAttendanceRate: 85.5 }
    MY_COURSES = data.courses.map(course => ({
      id: course.courseId || 0,
      name: course.courseName,
      teacher: course.teacherName,
      attendanceRate: course.attendanceRate || 0,
      presentCount: course.presentCount || 0,
      absentCount: course.absentCount || 0,
      lateCount: course.lateCount || 0,
      status: course.status || 'ACTIVE',
      days: [],
      time: '',
      img: images.code
    }));

    return MY_COURSES;
  } catch (error) {
    console.error('Failed to load my courses:', error);
    alert('수강 내역을 불러오는데 실패했습니다.');
    return [];
  }
}

// 수강 신청
async function enrollCourse(courseId) {
  try {
    await apiRequest(`/api/students/courses/${courseId}/enroll`, {
      method: 'POST'
    });
    return true;
  } catch (error) {
    console.error('Failed to enroll course:', error);
    throw error;
  }
}

// 수강 취소
async function cancelEnrollment(courseId) {
  try {
    await apiRequest(`/api/students/courses/${courseId}/enroll`, {
      method: 'DELETE'
    });
    return true;
  } catch (error) {
    console.error('Failed to cancel enrollment:', error);
    throw error;
  }
}

// 설문조사 목록 조회
async function loadSurveys() {
  try {
    const data = await apiRequest('/api/students/surveys');
    SURVEYS = data;
    return SURVEYS;
  } catch (error) {
    console.error('Failed to load surveys:', error);
    return [];
  }
}

// 설문조사 상세 조회
async function getSurveyDetail(surveyId) {
  try {
    const data = await apiRequest(`/api/students/surveys/${surveyId}`);
    return data;
  } catch (error) {
    console.error('Failed to load survey detail:', error);
    return null;
  }
}

// 설문조사 제출
async function submitSurvey(surveyId, responses) {
  try {
    await apiRequest(`/api/students/surveys/${surveyId}/responses`, {
      method: 'POST',
      body: JSON.stringify(responses)
    });
    return true;
  } catch (error) {
    console.error('Failed to submit survey:', error);
    throw error;
  }
}

// ===============================
// 유틸
// ===============================
const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

function remainSeats(c) { return Math.max(0, c.capacity - c.enrolled); }

// 수강 신청 여부 확인 (API에서 제공하는 isEnrolled 사용)
function isApplied(courseId) {
  const course = COURSES.find(c => c.id === courseId);
  return course ? course.isEnrolled : false;
}

// ===============================
// 렌더: 카드들
// ===============================
function courseCardHTML(c) {
  const badges = `<div class="badges">
      <span class="badge">${c.category}</span>
      ${c.tags.map(t => `<span class="badge">${t}</span>`).join("")}
    </div>`;
  const sched = `${c.days.join(", ")} ${c.time}`;
  return `
  <article class="course-card" data-id="${c.id}">
    <img class="course-img" src="${c.img}" alt="${c.name}">
    <div class="course-body">
      <h3>${c.name}</h3>
      <p>${c.teacher} · ${sched}</p>
      ${badges}
      <div class="kv"><span>정원</span><span><strong>${c.enrolled}</strong> / ${c.capacity}</span></div>
      <div class="kv"><span>여석</span><span><strong>${remainSeats(c)}</strong></span></div>
    </div>
  </article>
  `;
}

async function renderDashboard() {
  const grid = $("#dashboardGrid");

  // 강좌 목록이 없으면 로드
  if (COURSES.length === 0) {
    await loadCourses();
  }

  // 인기순(여석 적을수록, 신청자 많을수록 가정) 정렬 상위 6
  const popular = [...COURSES].sort((a, b) => (b.enrolled / b.capacity) - (a.enrolled / a.capacity)).slice(0, 6);
  grid.innerHTML = popular.map(courseCardHTML).join("");
}

function uniqueTeachers() {
  return Array.from(new Set(COURSES.map(c => c.teacher)));
}
function uniqueCategories() {
  return Array.from(new Set(COURSES.map(c => c.category)));
}

function buildFilters() {
  // 담당 교사 드롭다운
  const sel = $("#teacherFilter");
  const teachers = uniqueTeachers();
  sel.innerHTML = `<option value="">전체</option>` + teachers.map(t => `<option value="${t}">${t}</option>`).join("");

  // 카테고리 칩
  const chips = $("#categoryChips");
  const cats = uniqueCategories();
  chips.innerHTML = cats.map(c => `<div class="chip" data-cat="${c}">${c}</div>`).join("");
}

const filters = { teacher: "", cats: new Set(), search: "", sort: "popular" };

function applyFilters(list) {
  let arr = list;
  // 검색(강좌명/강사명)
  if (filters.search) {
    const q = filters.search.toLowerCase();
    arr = arr.filter(c => c.name.toLowerCase().includes(q) || c.teacher.toLowerCase().includes(q));
  }
  // 담당 교사
  if (filters.teacher) {
    arr = arr.filter(c => c.teacher === filters.teacher);
  }
  // 카테고리(여러 개 선택 시 OR)
  if (filters.cats.size > 0) {
    arr = arr.filter(c => filters.cats.has(c.category));
  }
  // 정렬
  switch (filters.sort) {
    case "name": arr.sort((a, b) => a.name.localeCompare(b.name)); break;
    case "teacher": arr.sort((a, b) => a.teacher.localeCompare(b.teacher)); break;
    case "category": arr.sort((a, b) => a.category.localeCompare(b.category) || a.name.localeCompare(b.name)); break;
    case "remain": arr.sort((a, b) => remainSeats(b) - remainSeats(a)); break;
    default: // popular
      arr.sort((a, b) => (b.enrolled / b.capacity) - (a.enrolled / a.capacity));
  }
  return arr;
}

async function renderAllCourses() {
  const grid = $("#allCoursesGrid");

  // 강좌 목록이 없으면 로드
  if (COURSES.length === 0) {
    await loadCourses();
  }

  const filtered = applyFilters([...COURSES]);
  grid.innerHTML = filtered.map(courseCardHTML).join("");
}

// ===============================
// 모달
// ===============================
let currentCourse = null;

function openCourseModal(courseId) {
  const c = COURSES.find(c => c.id === courseId);
  if (!c) return;
  currentCourse = c;

  // 채우기
  $("#modalImg").src = c.img;
  $("#courseModalTitle").textContent = c.name;
  $("#courseDesc").textContent = c.desc;
  $("#courseCategory").textContent = `${c.category}${c.tags.length ? " · " + c.tags.join(", ") : ""}`;
  $("#courseTeacher").textContent = c.teacher;
  $("#courseSchedule").textContent = `${c.days.join(", ")} ${c.time}`;
  $("#courseRoom").textContent = c.room;
  $("#courseEnrolled").textContent = c.enrolled;
  $("#courseCapacity").textContent = c.capacity;

  const btn = $("#btnToggleApply");
  const warning = $("#attendanceWarning");

  // API에서 제공하는 canEnroll 필드로 신청 가능 여부 판단
  const blocked = c.canEnroll === false;
  warning.classList.toggle("hidden", !blocked);

  const applied = c.isEnrolled;
  btn.textContent = applied ? "수강 취소" : "수강 신청";
  btn.disabled = blocked;

  $("#courseModal").classList.remove("hidden");
}

function closeCourseModal() {
  $("#courseModal").classList.add("hidden");
  currentCourse = null;
}

async function toggleApply() {
  if (!currentCourse) return;

  const c = currentCourse;
  const applied = isApplied(c.id);

  const btn = $("#btnToggleApply");
  btn.disabled = true;

  try {
    if (applied) {
      if (!confirm(`'${c.name}' 수강을 취소할까요?`)) {
        btn.disabled = false;
        return;
      }

      // API 호출: 수강 취소
      await cancelEnrollment(c.id);
      alert('수강 신청이 취소되었습니다.');

      // 내 수강 목록에서 제거
      MY_COURSES = MY_COURSES.filter(course => course.id !== c.id);

    } else {
      if (remainSeats(c) <= 0) {
        alert("정원이 가득 찼습니다.");
        btn.disabled = false;
        return;
      }

      if (!confirm(`'${c.name}' 수강을 신청할까요?`)) {
        btn.disabled = false;
        return;
      }

      // API 호출: 수강 신청
      await enrollCourse(c.id);
      alert('수강 신청이 완료되었습니다.');

      // 내 수강 목록에 추가 (임시)
      MY_COURSES.push({
        id: c.id,
        name: c.name,
        teacher: c.teacher,
        attendanceRate: 0
      });
    }

    // 모달 닫기
    closeCourseModal();

    // 리스트 갱신
    await loadCourses(); // 강좌 목록 다시 로드 (enrolled 수 업데이트)
    await renderDashboard();
    await renderAllCourses();
    await renderMyApplies();

  } catch (error) {
    alert(error.message || '처리 중 오류가 발생했습니다.');
  } finally {
    btn.disabled = false;
  }
}

// ===============================
// 내 신청 내역 + 출석률
// ===============================
function applyCardHTML(c, status) {
  const sched = `${c.days.join(", ")} ${c.time}`;
  const stCls = status === 'active' ? 'ongoing' : 'canceled';
  const stTxt = status === 'active' ? '수강중' : '취소됨';
  const canCancel = status === 'active';
  return `
  <div class="apply-card">
    <img class="apply-thumb" src="${c.img}" alt="${c.name}">
    <div class="apply-info">
      <div class="apply-top">
        <div>
          <h3>${c.name}</h3>
          <p>${sched}</p>
        </div>
        <span class="status ${stCls}">${stTxt}</span>
      </div>
      ${canCancel ? `<button class="apply-btn ghost" data-cancel="${c.id}">신청 취소</button>` : ""}
    </div>
  </div>`;
}

async function renderMyApplies() {
  const box = $("#applyList");

  // API에서 내 수강 내역 로드
  await loadMyCourses();

  if (MY_COURSES.length === 0) {
    box.innerHTML = `<div class="hint">신청한 강좌가 없습니다.</div>`;
    return;
  }

  const html = MY_COURSES.map(c => {
    const sched = c.days.length > 0 ? `${c.days.join(", ")} ${c.time}` : '시간 미정';
    return `
    <div class="apply-card">
      <img class="apply-thumb" src="${c.img}" alt="${c.name}">
      <div class="apply-info">
        <div class="apply-top">
          <div>
            <h3>${c.name}</h3>
            <p>${c.teacher} · ${sched}</p>
          </div>
          <span class="status ongoing">수강중</span>
        </div>
        <button class="apply-btn ghost" data-cancel="${c.id}">신청 취소</button>
      </div>
    </div>`;
  }).join("");

  box.innerHTML = html;

  // 취소 버튼 핸들
  $$("#applyList [data-cancel]").forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.getAttribute('data-cancel');
      const c = MY_COURSES.find(x => x.id == id);
      if (!c) return;

      if (confirm(`'${c.name}' 수강을 취소할까요?`)) {
        try {
          btn.disabled = true;
          await cancelEnrollment(id);
          alert('수강 신청이 취소되었습니다.');

          await loadCourses();
          await renderMyApplies();
          await renderAllCourses();
          await renderDashboard();
        } catch (error) {
          alert(error.message || '취소 처리 중 오류가 발생했습니다.');
          btn.disabled = false;
        }
      }
    });
  });
}

function drawBarChart(canvas, { present, absent, late }) {
  const ctx = canvas.getContext("2d");
  const W = canvas.width, H = canvas.height;
  ctx.clearRect(0, 0, W, H);

  // 값
  const vals = [present, absent, late];
  const labels = ["출석", "결석", "지각"];
  const max = Math.max(1, ...vals);
  const barW = Math.floor(W / (vals.length * 2));
  const gap = barW;

  // 축
  ctx.strokeStyle = "#e5e7eb";
  ctx.beginPath();
  ctx.moveTo(30, 10); ctx.lineTo(30, H - 25); ctx.lineTo(W - 10, H - 25);
  ctx.stroke();

  // 막대
  for (let i = 0; i < vals.length; i++) {
    const x = 30 + gap + i * (barW + gap);
    const h = Math.round((H - 45) * (vals[i] / max));
    const y = (H - 25) - h;
    ctx.fillStyle = "#3B82F6";
    ctx.fillRect(x, y, barW, h);
    // 라벨
    ctx.fillStyle = "#6B7280";
    ctx.font = "12px sans-serif";
    ctx.fillText(labels[i], x, H - 8);
  }
}

async function renderAttendance() {
  const wrap = $("#attendanceWrap");

  // API에서 내 수강 내역 로드
  if (MY_COURSES.length === 0) {
    await loadMyCourses();
  }

  if (MY_COURSES.length === 0) {
    wrap.innerHTML = `<div class="hint">수강 중인 강좌의 출석 데이터가 없습니다.</div>`;
    return;
  }

  wrap.innerHTML = MY_COURSES.map(c => {
    const rate = Math.round(c.attendanceRate);
    return `
      <div class="att-card">
        <div class="att-head">
          <h4>${c.name}</h4>
          <span class="small">출석률 ${rate}%</span>
        </div>
        <div style="padding: 20px; text-align: center; color: #6B7280;">
          출석 상세 데이터는 API에서 제공되지 않습니다.
        </div>
      </div>`;
  }).join("");
}

// ===============================
// 설문
// ===============================
function renderSurveyList(mode = "all") {
  const list = $("#surveyList");
  const myCourseIds = new Set(APPLICATIONS.filter(a => a.status === 'active').map(a => a.courseId));
  const pool = SURVEYS.filter(sv => mode === "all" ? true : (sv.courseId ? myCourseIds.has(sv.courseId) : false));

  if (pool.length === 0) {
    list.innerHTML = `<div class="hint">표시할 설문이 없습니다.</div>`;
    return;
  }
  list.innerHTML = pool.map(s => {
    const done = !!SURVEY_ANS[s.id]?.answered;
    return `
    <div class="survey-card" data-survey="${s.id}">
      <div class="survey-meta">
        <strong>${s.title}</strong>
        <span class="small">기간 ${s.period}${s.courseId ? ` · 대상강좌 ${s.courseId}` : ""}</span>
      </div>
      ${done ? `<span class="badge-done">참여 완료</span>` : `<button class="primary" data-open="${s.id}">응답하기</button>`}
    </div>`;
  }).join("");

  // 열기 버튼 핸들
  $$('[data-open]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-open');
      openSurveyForm(id);
    });
  });
}

function openSurveyForm(surveyId) {
  const wrap = $("#surveyFormWrap");
  const sv = SURVEYS.find(x => x.id === surveyId);
  if (!sv) return;

  // 이미 참여완료면 막기
  if (SURVEY_ANS[sv.id]?.answered) {
    alert("이미 참여하신 설문입니다.");
    return;
  }

  const qHTML = sv.questions.map(q => {
    if (q.type === "single") {
      return `
        <div class="q">
          <h4>${q.text}</h4>
          <div class="opts">
            ${q.options.map((op, i) => `
              <label>
                <input type="radio" name="${q.id}" value="${op}"> ${op}
              </label>`).join("")}
          </div>
        </div>`;
    } else {
      return `
        <div class="q">
          <h4>${q.text}</h4>
          <textarea name="${q.id}" rows="4" placeholder="자유롭게 작성하세요"></textarea>
        </div>`;
    }
  }).join("");

  wrap.innerHTML = `
    <div class="section-head"><h3>${sv.title}</h3><div class="hint">${sv.period}</div></div>
    <form id="surveyForm" data-id="${sv.id}">
      ${qHTML}
      <div style="display:flex;gap:8px;margin-top:10px">
        <button type="submit" class="primary">제출하기</button>
        <button type="button" class="ghost" id="closeSurvey">닫기</button>
      </div>
    </form>
  `;
  wrap.classList.remove("hidden");

  $("#closeSurvey").addEventListener('click', () => wrap.classList.add("hidden"));
  $("#surveyForm").addEventListener('submit', (e) => {
    e.preventDefault();
    // 값 수집
    const form = e.target;
    const data = {};
    let valid = true;
    SURVEYS.find(s => s.id === surveyId).questions.forEach(q => {
      if (q.type === "single") {
        const v = form.querySelector(`input[name="${q.id}"]:checked`);
        if (!v) valid = false;
        else data[q.id] = v.value;
      } else {
        const v = form.querySelector(`textarea[name="${q.id}"]`).value.trim();
        data[q.id] = v;
      }
    });
    if (!valid) { alert("모든 객관식 문항을 선택해주세요."); return; }

    SURVEY_ANS[sv.id] = { answered: true, data, ts: Date.now() };
    saveSurveyAnswers(SURVEY_ANS);
    alert("제출되었습니다. 감사합니다!");
    wrap.classList.add("hidden");
    renderSurveyList($(".tab.active").dataset.tab);
  });
}

// ===============================
// 이벤트 & 초기화
// ===============================
function bindGlobalEvents() {
  // 메뉴 전환
  const links = $$('.menu a[data-target]');
  const sections = $$('.page-section');
  links.forEach(link => {
    link.addEventListener('click', e => {
      e.preventDefault();
      const target = link.getAttribute('data-target');

      sections.forEach(sec => sec.classList.add('hidden'));
      document.getElementById(target).classList.remove('hidden');
      links.forEach(l => l.classList.remove('active'));
      link.classList.add('active');

      // 섹션 진입 시 필요한 렌더
      if (target === 'dashboard') renderDashboard();
      if (target === 'allCourses') renderAllCourses();
      if (target === 'apply') { renderMyApplies(); renderAttendance(); }
      if (target === 'survey') { renderSurveyList($(".tab.active").dataset.tab); }
    });
  });

  // 전역 검색
  $("#globalSearch").addEventListener('input', (e) => {
    filters.search = e.target.value.trim();
    renderAllCourses();
    // 대시보드엔 검색 미적용 (명세상 전체 강좌 탐색 위주)
  });

  // 담당 교사 필터
  $("#teacherFilter").addEventListener('change', (e) => {
    filters.teacher = e.target.value;
    renderAllCourses();
  });

  // 정렬
  $("#sortSelect").addEventListener('change', (e) => {
    filters.sort = e.target.value;
    renderAllCourses();
  });

  // 카테고리 칩 (토글 멀티선택)
  $("#categoryChips").addEventListener('click', (e) => {
    const chip = e.target.closest('.chip');
    if (!chip) return;
    chip.classList.toggle('active');
    const cat = chip.getAttribute('data-cat');
    if (chip.classList.contains('active')) filters.cats.add(cat);
    else filters.cats.delete(cat);
    renderAllCourses();
  });

  // 카드 클릭 → 모달
  document.addEventListener('click', (e) => {
    const card = e.target.closest('.course-card');
    if (card) {
      const id = card.getAttribute('data-id');
      openCourseModal(id);
    }
  });

  // 모달 버튼들
  $("#modalClose").addEventListener('click', closeCourseModal);
  $("#btnCancel").addEventListener('click', closeCourseModal);
  $("#btnToggleApply").addEventListener('click', toggleApply);

  // 설문 탭
  $$("#survey .tab").forEach(tab => {
    tab.addEventListener('click', () => {
      $$("#survey .tab").forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      renderSurveyList(tab.dataset.tab);
      $("#surveyFormWrap").classList.add("hidden");
    });
  });

  // ESC로 모달 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === "Escape" && !$("#courseModal").classList.contains("hidden")) closeCourseModal();
  });

  // 로그아웃 버튼
  $("#btnLogout").addEventListener('click', logout);
}

async function init() {
  // 초기 데이터 로드
  await loadCourses();
  buildFilters();
  await renderDashboard();
  await renderAllCourses();
  // 내신청/설문은 탭 들어갈 때마다 갱신하도록 함
}

bindGlobalEvents();
init();

