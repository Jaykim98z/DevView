(function () {
  const form = document.querySelector('form[th\\:object], form[action*="/community/posts/interview"]') || document.querySelector('form');
  if (!form) return;

  const el = {
    resultId: document.getElementById('interviewResultId'),
    grade: document.getElementById('grade'),
    score: document.getElementById('score'),
    feedback: document.getElementById('interviewFeedback'),
    title: document.getElementById('title'),
    memo: document.getElementById('content'),         // freePost.content
    memoCounter: document.getElementById('memo-counter'),
    submit: document.getElementById('submitBtn')
  };

  const isBlank = (v) => !v || !String(v).trim();
  const toNum = (v) => Number(v ?? NaN);

  const updateMemoCounter = () => {
    if (!el.memo || !el.memoCounter) return;
    const len = (el.memo.value || '').length;
    el.memoCounter.textContent = String(len);
  };
  el.memo?.addEventListener('input', updateMemoCounter);
  updateMemoCounter();

  el.score?.addEventListener('input', () => {
    const n = toNum(el.score.value);
    if (Number.isNaN(n)) return;
    if (n < 0) el.score.value = 0;
    if (n > 100) el.score.value = 100;
  });

  // 제출 검증
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

  document.getElementById('title')?.addEventListener('keydown', (ev) => {
    if (ev.key === 'Enter') ev.preventDefault();
  });

  (function focusFirst() {
    (el.resultId || el.title || el.memo)?.focus({ preventScroll: true });
  })();
})();
