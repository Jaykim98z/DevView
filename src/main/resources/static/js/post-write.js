(function () {
  console.log('[post-write] boot');

  const API_LATEST = '/api/community/interview-results/latest';
  const API_BY_ID = (id) => `/api/community/interview-results/${encodeURIComponent(id)}`;
  const API_CREATE = '/api/community/posts/interview';

  function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return { token, header };
  }
  function jsonHeaders() {
    const h = { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' };
    const { token, header } = getCsrf();
    if (token && header) h[header] = token;
    return h;
  }
  const isBlank = (v) => v == null || String(v).trim() === '';
  const toNum = (v) => Number(v ?? NaN);

  const form =
    document.getElementById('community-post-form') ||
    document.querySelector('form[action*="/community/posts/interview"]') ||
    document.querySelector('form');
  if (!form) { console.warn('[post-write] form not found'); return; }

  const el = {
    resultId: document.getElementById('interviewResultId'),
    grade: document.getElementById('grade'),
    score: document.getElementById('score'),
    feedback: document.getElementById('interviewFeedback'),
    title: document.getElementById('title'),
    memo: document.getElementById('content'),
    memoCounter: document.getElementById('memo-counter'),
    fpType: document.getElementById('fpType'),
    fpGrade: document.getElementById('fpGrade'),
    submit: document.getElementById('submitBtn'),
  };

  if (!el.resultId) {
    const hidden = document.createElement('input');
    hidden.type = 'hidden';
    hidden.id = 'interviewResultId';
    hidden.name = 'interviewResultId';
    form.appendChild(hidden);
    el.resultId = hidden;
  }

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

  async function fetchJson(url, init) {
    const res = await fetch(url, { credentials: 'same-origin', headers: { 'X-Requested-With': 'XMLHttpRequest' }, ...init });
    if (res.status === 204) return null;
    const text = await res.text().catch(() => '');
    if (!res.ok) throw new Error(text || `HTTP ${res.status}`);
    try { return text ? JSON.parse(text) : null; } catch { return null; }
  }

  async function fetchResultById(resultId) {
    console.log('[post-write] fetch by id:', resultId);
    return fetchJson(API_BY_ID(resultId));
  }
  async function fetchLatestResult() {
    console.log('[post-write] fetch latest');
    try {
      const data = await fetchJson(API_LATEST);
      console.log('[post-write] latest response:', data);
      return data;
    } catch (e) {
      console.warn('[post-write] latest fetch failed:', e.message);
      return null;
    }
  }

  function setIfBlank(input, val) {
    if (!input) return;
    if (isBlank(input.value) && !isBlank(val)) input.value = String(val);
  }
  function fillFormFromResult(d) {
    if (!d || typeof d !== 'object') return;
    setIfBlank(el.resultId, d.resultId ?? d.id);
    if (el.grade && d.grade != null && isBlank(el.grade.value)) {
      const val = String(d.grade);
      if (el.grade.querySelector(`option[value="${val}"]`)) el.grade.value = val;
    }
    if (el.score && d.totalScore != null && isBlank(el.score.value)) {
      const n = Number(d.totalScore);
      if (!Number.isNaN(n)) el.score.value = Math.max(0, Math.min(100, n));
    }
    setIfBlank(el.feedback, d.feedback);
    if (el.title && isBlank(el.title.value)) {
      const g = d.grade ?? '';
      const s = d.totalScore ?? '';
      const base = `[면접결과] ${g}${g ? ' ' : ''}${s !== '' ? `${s}점` : ''}`.trim();
      if (base) el.title.value = base;
    }
    updateMemoCounter();
  }

  (async function bootPrefill() {
    try {
      const params = new URLSearchParams(location.search);
      const rid = params.get('interviewResultId');
      if (rid) {
        const data = await fetchResultById(rid);
        fillFormFromResult(data);
      } else {
        const latest = await fetchLatestResult();
        fillFormFromResult(latest);
      }
    } finally {}
  })();

  function validate() {
    if (!el.resultId || isBlank(el.resultId.value)) { alert('인터뷰 결과 ID가 없습니다.'); el.resultId?.focus(); return false; }
    if (!el.grade || isBlank(el.grade.value)) { alert('등급을 선택하세요.'); el.grade?.focus(); return false; }
    const score = toNum(el.score?.value);
    if (Number.isNaN(score) || score < 0 || score > 100) { alert('점수는 0~100 사이여야 합니다.'); el.score?.focus(); return false; }
    if (!el.feedback || isBlank(el.feedback.value)) { alert('상세 피드백을 입력하세요.'); el.feedback?.focus(); return false; }
    if (!el.title || isBlank(el.title.value)) { alert('제목을 입력하세요.'); el.title?.focus(); return false; }
    if (el.memo && el.memo.value.length > 1000) { alert('추가 메모는 1000자 이내여야 합니다.'); el.memo?.focus(); return false; }
    return true;
  }
  function lock(){ if (el.submit){ el.submit.disabled = true; el.submit.textContent = '등록 중...'; } }
  function unlock(){ if (el.submit){ el.submit.disabled = false; el.submit.textContent = '등록'; } }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (el.submit?.disabled) return;
    if (!validate()) return;

    const payloadFlat = {
      interviewResultId: Number(el.resultId.value),
      grade: el.grade.value,
      score: Number(el.score.value),
      interviewFeedback: el.feedback.value,
      title: el.title.value,
      content: el.memo?.value || '',
      interviewType: el.fpType?.value || 'PRACTICE'
    };

    const payloadV1 = {
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

    const payloadV2 = {
      interviewShare: {
        interviewResultId: Number(el.resultId.value),
        grade: el.grade.value,
        totalScore: Number(el.score.value),
        feedback: el.feedback.value,
        title: el.title.value,
      },
      freePost: {
        interviewType: el.fpType?.value || 'PRACTICE',
        grade: el.fpGrade?.value || 'C',
        content: el.memo?.value || '',
      },
    };

    async function tryPost(bodyObj) {
      const res = await fetch(API_CREATE, {
        method: 'POST',
        headers: jsonHeaders(),
        credentials: 'same-origin',
        body: JSON.stringify(bodyObj),
      });
      const text = await res.text().catch(() => '');
      let data = null; try { data = text ? JSON.parse(text) : null; } catch {}
      if (!res.ok) {
        console.error('[create] 400 body:', bodyObj);
        console.error('[create] 400 resp:', res.status, data || text);
        const err = new Error(text || `HTTP ${res.status}`); err.status = res.status; err.data = data; err.text = text; throw err;
      }
      return data;
    }
    function hasFieldError(errObj, fieldPath) {
      const errors = errObj?.data?.errors;
      if (!Array.isArray(errors)) return false;
      return errors.some(e => e.field === fieldPath);
    }

    lock();
    try {
      let saved;
      try { saved = await tryPost(payloadFlat); }
      catch (e0) {
        if (e0.status === 400 && (hasFieldError(e0, 'interviewShare') || e0.text?.includes('interviewShare'))) {
          saved = await tryPost(payloadV1);
        } else if (e0.status === 400 && (e0.text?.includes('totalScore') || e0.text?.includes('feedback'))) {
          saved = await tryPost(payloadV2);
        } else {
          const msg = e0.data?.errors?.map(er => `${er.field}: ${er.message}`).join('\n') || e0.text || `등록 실패 (${e0.status})`;
          alert(msg);
          throw e0;
        }
      }
      const postId = saved?.postId ?? saved?.id ?? null;
      window.location.href = postId ? `/community/posts/${postId}/detail` : '/community';
    } catch (err) {
      if (err.status === 401) alert('로그인이 필요합니다.');
      else if (err.status === 403) alert('보안 토큰(CSRF) 오류입니다. 새로고침 후 다시 시도해주세요.');
      else if (err.status && err.status !== 400) alert(`등록 실패 (${err.status})`);
    } finally {
      unlock();
    }
  });

  el.title?.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') ev.preventDefault(); });
})();
