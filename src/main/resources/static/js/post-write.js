(function () {
  const API_LATEST = '/api/community/interview-results/latest';
  const API_BY_ID  = (id) => `/api/community/interview-results/${encodeURIComponent(id)}`;
  const API_COMPOSE = '/api/community/posts/compose';

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
      resultId:     document.getElementById('interviewResultId'),
      title:        document.getElementById('title'),
      memo:         document.getElementById('content'),
      memoCounter:  document.getElementById('memo-counter'),
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

  function buildPayload() {
    const el = $els();
    const interviewResultId =
      el.resultId && !isBlank(el.resultId.value) ? Number(el.resultId.value) : null;

    return {
      category: 'INTERVIEW_SHARE',
      interviewShare: {
        interviewResultId,
        title: (el.title?.value ?? '').trim(),
        content: el.memo?.value ?? ''
      }
    };
  }

  async function createPost(payload) {
    const { token, header } = getCsrf();
    const headers = { 'Content-Type': 'application/json' };
    if (token && header) headers[header] = token;

    return fetchJson(API_COMPOSE, {
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

    el.title?.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') ev.preventDefault(); });

    el.form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      if (el.submit) el.submit.disabled = true;

      try {
        const payload = buildPayload();
        if (!payload.interviewShare?.interviewResultId) { alert('인터뷰 결과 ID를 입력하세요.'); return; }
        if (isBlank(payload.interviewShare?.title)) { alert('제목을 입력해주세요.'); return; }

        const res = await createPost(payload);
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
