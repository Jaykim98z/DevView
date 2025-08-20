(function () {
  const API_LATEST = '/api/community/interview-results/latest';
  const API_BY_ID  = (id) => `/api/community/interview-results/${encodeURIComponent(id)}`;
  const API_CREATE_FREE  = '/community/posts';
  const API_CREATE_SHARE = '/community/posts/interview-share';

  const isBlank = (v) => v == null || String(v).trim() === '';
  const toNum   = (v) => Number(v ?? NaN);

  function getForm() {
    return (
      document.getElementById('community-post-form') ||
      document.querySelector('form[action*="/community/posts"]') ||
      document.querySelector('form')
    );
  }

  function $els() {
    return {
      form:         getForm(),
      postCategory: document.getElementById('postCategory'),
      resultId:     document.getElementById('interviewResultId'),
      grade:        document.getElementById('grade'),
      score:        document.getElementById('score'),
      feedback:     document.getElementById('interviewFeedback'),
      title:        document.getElementById('title'),
      memo:         document.getElementById('content'),
      memoCounter:  document.getElementById('memo-counter'),
      fpType:       document.getElementById('fpType'),
      fpGrade:      document.getElementById('fpGrade'),
      techTag:      document.getElementById('techTag'),
      type:         document.getElementById('type'),
      submit:       document.getElementById('submitBtn'),
    };
  }

  function getCsrf() {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    return { token, header };
  }

  async function fetchJson(url, init) {
    const res = await fetch(url, {
      credentials: 'same-origin',
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
      ...init,
    });
    const text = await res.text().catch(() => '');
    if (!res.ok) throw new Error(text || `HTTP ${res.status}`);
    try { return text ? JSON.parse(text) : null; } catch { return null; }
  }

  async function fetchResultById(resultId)  { return fetchJson(API_BY_ID(resultId)); }
  async function fetchLatestResult()        { return fetchJson(API_LATEST); }

  function updateMemoCounter(el) {
    if (!el.memo || !el.memoCounter) return;
    el.memoCounter.textContent = String((el.memo.value || '').length);
  }

  function setIfBlank(input, val) {
    if (!input) return;
    if (isBlank(input.value) && !isBlank(val)) input.value = String(val);
  }

  function fillFormFromResult(data) {
    if (!data || typeof data !== 'object') return;
    const el = $els();
    setIfBlank(el.resultId, data.resultId ?? data.id);
    if (el.grade && data.grade != null && isBlank(el.grade.value)) {
      const val = String(data.grade);
      if (el.grade.querySelector(`option[value="${val}"]`)) el.grade.value = val;
    }
    if (el.score && data.totalScore != null && isBlank(el.score.value)) {
      const n = Number(data.totalScore);
      if (!Number.isNaN(n)) el.score.value = Math.max(0, Math.min(100, n));
    }
    if (el.feedback && isBlank(el.feedback.value) && data.feedback != null) {
      el.feedback.value = String(data.feedback);
    }
    if (el.title && isBlank(el.title.value)) {
      const g = data.grade ?? '';
      const s = data.totalScore ?? '';
      const base = `[면접결과] ${g}${g ? ' ' : ''}${s !== '' ? `${s}점` : ''}`.trim();
      if (base) el.title.value = base;
    }
    if (el.memo && isBlank(el.memo.value)) {
      el.memo.value = '';
      updateMemoCounter(el);
    }
  }

  (async function bootPrefill() {
    try {
      const params = new URLSearchParams(location.search);
      const rid = params.get('interviewResultId');
      let data = null;
      if (rid && !isBlank(rid)) data = await fetchResultById(rid);
      else data = await fetchLatestResult();
      fillFormFromResult(data);
    } catch (e) {}
  })();

  function buildPayloadAndEndpoint() {
    const el = $els();

    const interviewResultId =
      el.resultId && !isBlank(el.resultId.value) ? Number(el.resultId.value) : null;
    const scoreNum =
      el.score && !isBlank(el.score.value)
        ? Math.max(0, Math.min(100, toNum(el.score.value)))
        : null;

    let category = el.postCategory?.value?.trim()?.toUpperCase();
    if (isBlank(category)) {
      const hasInterviewSignals =
        interviewResultId != null ||
        scoreNum != null ||
        !isBlank(el.grade?.value) ||
        !isBlank(el.feedback?.value);
      category = hasInterviewSignals ? 'INTERVIEW_SHARE' : 'FREE';
    }

    const common = {
      title: (el.title?.value ?? '').trim(),
      content: el.memo?.value ?? ''
    };

    if (category === 'INTERVIEW_SHARE') {
      const payload = {
        ...common,
        interviewResultId: interviewResultId ?? 0,
        grade: el.grade?.value ?? '',
        score: Number.isFinite(scoreNum) ? scoreNum : 0,
        interviewFeedback: el.feedback?.value ?? ''
      };
      if (!isBlank(el.fpType?.value)) payload.interviewType = el.fpType.value;
      return { endpoint: API_CREATE_SHARE, payload };
    }

    const payload = {
      ...common,
      interviewType: el.fpType?.value ?? '',
      grade: el.grade?.value ?? '',
      techTag: el.techTag?.value ?? '',
      level: el.fpGrade?.value ?? '',
      category: 'FREE',
      type: el.type?.value ?? '',
      score: Number.isFinite(scoreNum) ? scoreNum : null
    };

    return { endpoint: API_CREATE_FREE, payload };
  }

  async function createPost(endpoint, payload) {
    const { token, header } = getCsrf();
    const headers = { 'Content-Type': 'application/json' };
    if (token && header) headers[header] = token;

    return fetchJson(endpoint, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload)
    });
  }

  function bindOnceWhenFormReady() {
    const el = $els();
    if (!el.form) return false;

    el.resultId?.addEventListener('blur', async () => {
      if (!el.resultId || isBlank(el.resultId.value)) return;
      try { fillFormFromResult(await fetchResultById(el.resultId.value)); } catch {}
    });

    el.memo?.addEventListener('input', () => updateMemoCounter(el));
    updateMemoCounter(el);

    el.score?.addEventListener('input', () => {
      const n = toNum(el.score.value);
      if (Number.isNaN(n)) return;
      if (n < 0) el.score.value = 0;
      if (n > 100) el.score.value = 100;
    });

    el.title?.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') ev.preventDefault(); });

    el.form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      if (el.submit) el.submit.disabled = true;

      try {
        const { endpoint, payload } = buildPayloadAndEndpoint();
        if (isBlank(payload.title)) { alert('제목을 입력해주세요.'); return; }

        if (endpoint === API_CREATE_SHARE) {
          if (!payload.interviewResultId) { alert('interviewResultId가 필요합니다.'); return; }
          if (isBlank(payload.grade)) { alert('grade를 선택해주세요.'); return; }
          if (isBlank(payload.interviewFeedback)) { alert('interviewFeedback을 입력해주세요.'); return; }
        } else {
          if (isBlank(payload.grade)) { alert('grade를 선택해주세요.'); return; }
          if (isBlank(payload.interviewType)) { alert('interviewType을 선택해주세요.'); return; }
        }

        const res = await createPost(endpoint, payload);
        const pid = res?.id ?? res?.postId;
        if (pid) location.href = `/community/posts/${pid}`;
        else     location.href = '/community';
      } catch (e) {
        alert(`등록 실패: ${e.message ?? e}`);
      } finally {
        if (el.submit) el.submit.disabled = false;
      }
    }, { once: true });

    return true;
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      if (!bindOnceWhenFormReady()) {
        const mo = new MutationObserver(() => { if (bindOnceWhenFormReady()) mo.disconnect(); });
        mo.observe(document.body, { childList: true, subtree: true });
      }
    });
  } else {
    if (!bindOnceWhenFormReady()) {
      const mo = new MutationObserver(() => { if (bindOnceWhenFormReady()) mo.disconnect(); });
      mo.observe(document.body, { childList: true, subtree: true });
    }
  }
})();
