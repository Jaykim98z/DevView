(function () {

  const FETCH_URL = '/api/v1/interviews/latest/result';

  const form =
    document.querySelector('form[th\\:action], form[action*="/community/posts/interview"]') ||
    document.querySelector('form');
  if (!form) return;

  // DOM 캐싱 (템플릿 id와 1:1 매핑)
  const el = {
    resultId: document.getElementById('interviewResultId'),
    grade: document.getElementById('grade'),
    score: document.getElementById('score'),
    feedback: document.getElementById('interviewFeedback'),
    title: document.getElementById('title'),
    memo: document.getElementById('content'),
    memoCounter: document.getElementById('memo-counter'),
    submit: document.getElementById('submitBtn'),
  };

  // 유틸
  const isBlank = (v) => !v || !String(v).trim();
  const toNum = (v) => Number(v ?? NaN);

  // ---------------------------
  // 1) 인터뷰 결과 자동 불러오기
  // ---------------------------

  // (A) API 호출 - 기존 컨트롤러 경로 사용
  // GET /api/v1/interviews/results/{resultId}
  async function fetchResult(resultId) {
    const res = await fetch(`/api/v1/interviews/results/${encodeURIComponent(resultId)}`, {
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
    });
    if (!res.ok) throw new Error('결과 조회 실패');
    // { resultId, interviewId, totalScore, grade, feedback, recommendedResource }
    return res.json();
  }

  // (B) 폼 채우기 (비어있는 칸만 덮어씀)
  function fillFormFromResult(d) {
    if (!d) return;

    // 결과 ID
    if (el.resultId && isBlank(el.resultId.value) && d.resultId != null) {
      el.resultId.value = d.resultId;
    }

    // 등급 (select)
    if (el.grade && d.grade != null) {
      const val = String(d.grade); // 'A' ~ 'F'
      const opt = el.grade.querySelector(`option[value="${val}"]`);
      if (opt) el.grade.value = val;
    }

    // 점수 (0~100 가드)
    if (el.score && d.totalScore != null) {
      const n = Number(d.totalScore);
      if (!Number.isNaN(n)) {
        el.score.value = Math.max(0, Math.min(100, n));
      }
    }

    // 상세 피드백 (사용자가 이미 입력했으면 보존)
    if (el.feedback && isBlank(el.feedback.value) && d.feedback != null) {
      el.feedback.value = d.feedback;
    }

    // 제목 자동 추천 (비어있을 때만)
    if (el.title && isBlank(el.title.value)) {
      const g = d.grade ?? '';
      const s = d.totalScore ?? '';
      el.title.value = `[면접결과] ${g}${g ? ' ' : ''}${s !== '' ? `${s}점` : ''}`.trim();
    }

    // 추가 메모: 추천자료가 있고 메모가 비었을 때만
    if (el.memo && isBlank(el.memo.value) && d.recommendedResource) {
      el.memo.value = `추천 자료: ${d.recommendedResource}`;
      updateMemoCounter();
    }
  }

  // (C) 로딩 트리거들
  async function tryLoadById(id) {
    if (!id || isBlank(id)) return;
    try {
      const data = await fetchResult(id);
      fillFormFromResult(data);
    } catch (e) {
      console.error(e);
      alert('인터뷰 결과를 불러오지 못했습니다. ID와 권한을 확인해 주세요.');
    }
  }

  // URL ?interviewResultId=### 로 진입하면 자동 로드
  (function loadFromQuery() {
    const params = new URLSearchParams(location.search);
    const rid = params.get('interviewResultId');
    if (rid) {
      if (el.resultId && isBlank(el.resultId.value)) el.resultId.value = rid;
      tryLoadById(rid);
    }
  })();

  // 결과 ID 직접 입력 후 포커스 아웃 시 자동 로드
  el.resultId?.addEventListener('blur', () => tryLoadById(el.resultId.value));

  // ---------------------------
  // 2) UX 보강
  // ---------------------------

  function updateMemoCounter() {
    if (!el.memo || !el.memoCounter) return;
    el.memoCounter.textContent = String((el.memo.value || '').length);
  }
  el.memo?.addEventListener('input', updateMemoCounter);
  updateMemoCounter();

  el.score?.addEventListener('input', () => {
    const n = toNum(el.score.value);
    if (Number.isNaN(n)) return;
    if (n < 0) el.score.value = 0;
    if (n > 100) el.score.value = 100;
  });

  // ---------------------------
  // 3) 제출 검증/락
  // ---------------------------

  function validate() {
    if (!el.resultId || isBlank(el.resultId.value)) {
      alert('인터뷰 결과 ID를 입력하세요.');
      el.resultId?.focus();
      return false;
    }
    if (!el.grade || isBlank(el.grade.value)) {
      alert('등급을 선택하세요.');
      el.grade?.focus();
      return false;
    }
    const score = toNum(el.score?.value);
    if (Number.isNaN(score) || score < 0 || score > 100) {
      alert('점수는 0~100 사이여야 합니다.');
      el.score?.focus();
      return false;
    }
    if (!el.feedback || isBlank(el.feedback.value)) {
      alert('상세 피드백을 입력하세요.');
      el.feedback?.focus();
      return false;
    }
    if (!el.title || isBlank(el.title.value)) {
      alert('제목을 입력하세요.');
      el.title?.focus();
      return false;
    }
    if (el.memo && el.memo.value.length > 1000) {
      alert('추가 메모는 1000자 이내여야 합니다.');
      el.memo?.focus();
      return false;
    }
    return true;
  }

  function lockSubmit() {
    if (!el.submit) return;
    el.submit.disabled = true;
    el.submit.textContent = '등록 중...';
  }

  form.addEventListener('submit', (e) => {
    if (el.submit?.disabled) { e.preventDefault(); return; }
    if (!validate()) { e.preventDefault(); return; }
    lockSubmit();
  });

  // 제목 입력에서 Enter 방지
  el.title?.addEventListener('keydown', (ev) => {
    if (ev.key === 'Enter') ev.preventDefault();
  });

  // 최초 포커스
  (function focusFirst() {
    (el.resultId || el.title || el.memo)?.focus({ preventScroll: true });
  })();
})();
