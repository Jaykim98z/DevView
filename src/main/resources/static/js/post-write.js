(function () {
  const API_CREATE = '/api/community/posts/interview';

  function getCsrf() {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return { token, header };
  }
  function jsonHeaders() {
    const h = { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' };
    const { token, header } = getCsrf();
    if (token && header) h[header] = token;
    return h;
  }

  const form =
    document.getElementById('community-post-form') ||
    document.querySelector('form[action*="/community/posts/interview"]') ||
    document.querySelector('form');
  if (!form) return;

  const el = {
    resultId:  document.getElementById('interviewResultId'),
    grade:     document.getElementById('grade'),
    score:     document.getElementById('score'),
    feedback:  document.getElementById('interviewFeedback'),
    title:     document.getElementById('title'),
    memo:      document.getElementById('content'),
    memoCounter: document.getElementById('memo-counter'),
    fpType:    document.getElementById('fpType'),
    fpGrade:   document.getElementById('fpGrade'),
    submit:    document.getElementById('submitBtn'),
  };

  function updateMemoCounter() {
    if (!el.memo || !el.memoCounter) return;
    el.memoCounter.textContent = String((el.memo.value || '').length);
  }
  el.memo?.addEventListener('input', updateMemoCounter);
  updateMemoCounter();

  el.score?.addEventListener('input', () => {
    const n = Number(el.score.value);
    if (Number.isNaN(n)) return;
    if (n < 0) el.score.value = 0;
    if (n > 100) el.score.value = 100;
  });

  function isBlank(v){ return v == null || String(v).trim() === ''; }
  function validate() {
    if (!el.resultId || isBlank(el.resultId.value)) { alert('인터뷰 결과 ID가 없습니다.'); el.resultId?.focus(); return false; }
    if (!el.grade || isBlank(el.grade.value))       { alert('등급을 선택하세요.'); el.grade?.focus(); return false; }
    const score = Number(el.score?.value);
    if (Number.isNaN(score) || score < 0 || score > 100) { alert('점수는 0~100 사이여야 합니다.'); el.score?.focus(); return false; }
    if (!el.feedback || isBlank(el.feedback.value)) { alert('상세 피드백을 입력하세요.'); el.feedback?.focus(); return false; }
    if (!el.title || isBlank(el.title.value))       { alert('제목을 입력하세요.'); el.title?.focus(); return false; }
    if (el.memo && el.memo.value.length > 1000)     { alert('추가 메모는 1000자 이내여야 합니다.'); el.memo?.focus(); return false; }
    return true;
  }

  function lockSubmit() { if (el.submit){ el.submit.disabled = true; el.submit.textContent = '등록 중...'; } }
  function unlockSubmit(){ if (el.submit){ el.submit.disabled = false; el.submit.textContent = '등록'; } }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (el.submit?.disabled) return;
    if (!validate()) return;

    const payload = {
      interviewShare: {
        interviewResultId: Number(el.resultId.value),
        grade: el.grade.value,
        score: Number(el.score.value),
        interviewFeedback: el.feedback.value,
        title: el.title.value,
      },
      freePost: {
        interviewType: el.fpType?.value || 'PRACTICE',
        grade: el.fpGrade?.value || 'C',
        content: el.memo?.value || '',
      },
    };

    lockSubmit();
    try {
      const res = await fetch(API_CREATE, {
        method: 'POST',
        headers: jsonHeaders(),
        credentials: 'same-origin',
        body: JSON.stringify(payload),
      });

      if (res.status === 401) { alert('로그인이 필요합니다.'); unlockSubmit(); return; }
      if (res.status === 403) { alert('보안 토큰(CSRF) 오류입니다. 새로고침 후 다시 시도해주세요.'); unlockSubmit(); return; }
      if (!res.ok) {
        const t = await res.text().catch(()=> '');
        console.error('등록 실패', res.status, t);
        alert(`등록 실패 (${res.status})`);
        unlockSubmit(); return;
      }

      const data = await res.json().catch(()=> ({}));
      const postId = data?.postId ?? data?.id ?? null;
      if (postId) {
        window.location.href = `/community/posts/${postId}/detail`;
      } else {
        window.location.href = '/community';
      }
    } catch (err) {
      console.error(err);
      alert('네트워크 오류로 등록에 실패했습니다.');
      unlockSubmit();
    }
  });

})();
