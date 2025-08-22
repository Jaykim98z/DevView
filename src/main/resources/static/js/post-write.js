'use strict';

(function () {
  const API_LATEST = '/api/community/interview-results/latest';
  const API_BY_ID  = (id) => `/api/community/interview-results/${encodeURIComponent(id)}`;
  const API_COMPOSE = '/api/community/posts/compose';
  const API_MINE = '/api/community/interview-results/mine';

  const isBlank = (v) => v == null || String(v).trim() === '';
  const toNum   = (v) => Number(v ?? NaN);

  let csrfToken = null;
  let csrfHeader = null;

  function initCsrfToken() {
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    csrfToken = csrfTokenMeta ? csrfTokenMeta.content : null;
    csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : null;
  }

  function buildSecureHeaders(includeContentType = true) {
    const headers = { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'application/json' };
    if (includeContentType) headers['Content-Type'] = 'application/json';
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    return headers;
  }

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
      memoCounter:  document.getElementById('memo-counter')
    };
  }

  function filterText(s, max = 1000) {
    if (s == null) return '';
    let t = String(s);
    t = t.replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, '');
    t = t.replace(/<\/?[^>]+>/g, '');
    t = t.replace(/\s+/g, ' ').trim();
    if (max > 0 && t.length > max) t = t.slice(0, max);
    return t;
  }

  async function fetchJson(url, init = {}) {
    const headers = buildSecureHeaders(init.method !== 'GET');
    const res = await fetch(url, { credentials: 'same-origin', ...init, headers: { ...headers, ...init.headers } });
    if (res.status === 403) { alert('보안 토큰이 만료되었습니다. 새로고침 해주세요.'); location.reload(); return null; }
    const ct = res.headers.get('content-type') || '';
    if (ct.includes('text/html')) { window.location.href = '/user/login'; return null; }
    if (!res.ok) { throw new Error(await res.text().catch(() => `HTTP ${res.status}`)); }
    const text = await res.text().catch(() => '');
    try { return text ? JSON.parse(text) : null; } catch { return null; }
  }

  async function fetchResultById(resultId)  { return fetchJson(API_BY_ID(resultId)); }
  async function fetchLatestResult()        { return fetchJson(API_LATEST); }
  async function fetchMineList()            { return fetchJson(API_MINE); }

  function updateMemoCounter(el) {
    if (!el.memo || !el.memoCounter) return;
    el.memoCounter.textContent = String((el.memo.value || '').length);
  }

  function setIfBlank(input, val) {
    if (!input) return;
    if (isBlank(input.value) && !isBlank(val)) input.value = String(val);
  }

   function resultLabel(data) {
    if (!data || typeof data !== 'object') return '선택';
    const id = data.resultId ?? data.id ?? '';
    return id !== '' ? `#${id}` : '선택';
  }

  function ensureOption(selectEl, value, label) {
    if (!selectEl) return;
    const exists = Array.from(selectEl.options).some(o => o.value == String(value));
    if (!exists) {
      const opt = document.createElement('option');
      opt.value = String(value);
      opt.textContent = String(label ?? value);
      selectEl.appendChild(opt);
    }
    selectEl.value = String(value);
  }

  function fillSelectWithMine(list) {
    const el = $els();
    if (!el.resultId || !Array.isArray(list)) return;
    el.resultId.innerHTML = '';
    const ph = document.createElement('option');
    ph.value = '';
    ph.textContent = '인터뷰 결과';
    el.resultId.appendChild(ph);
    list.forEach(item => {
      const id = item.resultId ?? item.id;
      if (id == null) return;
      const opt = document.createElement('option');
      opt.value = String(id);
      opt.textContent = resultLabel(item);
      el.resultId.appendChild(opt);
    });
  }

  function fillFormFromResult(data) {
    if (!data || typeof data !== 'object') return;
    const el = $els();

    const rid = data.resultId ?? data.id;
    if (el.resultId && rid != null && !Number.isNaN(Number(rid))) {
      ensureOption(el.resultId, rid, resultLabel(data));
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
      initCsrfToken();
      const params = new URLSearchParams(location.search);
      const rid = params.get('interviewResultId');

      let mine = null;
      try { mine = await fetchMineList(); } catch {}
      if (Array.isArray(mine) && mine.length > 0) {
        fillSelectWithMine(mine);
        if (rid) {
          const found = mine.find(m => String(m.resultId ?? m.id) === String(rid));
          if (found) fillFormFromResult(found);
        } else {
          fillFormFromResult(mine[0]);
        }
        return;
      }

      const data = rid && !isBlank(rid) ? await fetchResultById(rid) : await fetchLatestResult();
      fillFormFromResult(data);
    } catch (e) {
      console.error('프리필 오류:', e);
    }
  })();

  function buildPayload() {
    const el = $els();
    const title   = filterText(el.title?.value, 150);
    const memoTxt = filterText(el.memo?.value, 1000);
    if (isBlank(title)) throw new Error('제목을 입력해주세요.');

    const interviewResultId =
      el.resultId && !isBlank(el.resultId.value) ? toNum(el.resultId.value) : null;
    if (interviewResultId == null || Number.isNaN(interviewResultId)) {
      throw new Error('면접 결과를 선택해주세요.');
    }

    return {
      category: 'INTERVIEW_SHARE',
      interviewShare: {
        title: title,
        interviewResultId: interviewResultId,
        extraMemo: memoTxt
      },
      freePost: null
    };
  }

  document.addEventListener('DOMContentLoaded', function() {
    const el = $els();

    if (el.resultId) {
      el.resultId.addEventListener('input', () => {
        el.resultId.value = el.resultId.value.replace(/[^\d]/g, '');
      });
    }

    if (el.memo) {
      el.memo.addEventListener('input', () => {
        const cleaned = filterText(el.memo.value, 1000);
        if (cleaned !== el.memo.value) el.memo.value = cleaned;
        updateMemoCounter(el);
      });
      updateMemoCounter(el);
    }

    if (el.form) {
      el.form.addEventListener('submit', async function(e) {
        e.preventDefault();
        try {
          const payload = buildPayload();
          const submitBtn = document.getElementById('submitBtn');
          if (submitBtn) submitBtn.disabled = true;
          const result = await fetchJson(API_COMPOSE, { method: 'POST', body: JSON.stringify(payload) });
          if (result && result.postId) {
            alert('글이 성공적으로 작성되었습니다.');
            window.location.href = `/community/posts/${result.postId}/detail`;
          } else {
            throw new Error('글 작성에 실패했습니다.');
          }
        } catch (err) {
          console.error('글 작성 오류:', err);
          alert(err.message || '글 작성 중 오류가 발생했습니다.');
        } finally {
          const submitBtn = document.getElementById('submitBtn');
          if (submitBtn) submitBtn.disabled = false;
        }
      });
    }
  });
})();
