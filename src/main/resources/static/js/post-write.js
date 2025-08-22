(function () {
  const API_LATEST = '/api/community/interview-results/latest';
  const API_BY_ID  = (id) => `/api/community/interview-results/${encodeURIComponent(id)}`;
  const API_COMPOSE = '/api/community/posts/compose';

  const isBlank = (v) => v == null || String(v).trim() === '';
  const toNum   = (v) => Number(v ?? NaN);

  let csrfToken = null;
  let csrfHeader = null;

  function initCsrfToken() {
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    csrfToken = csrfTokenMeta ? csrfTokenMeta.content : null;
    csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : null;

    if (!csrfToken || !csrfHeader) {
      console.warn('CSRF 토큰이 페이지에 없습니다.');
    }
  }

  function buildSecureHeaders(includeContentType = true) {
    const headers = {
      'X-Requested-With': 'XMLHttpRequest',
      'Accept': 'application/json'
    };

    if (includeContentType) {
      headers['Content-Type'] = 'application/json';
    }

    if (csrfToken && csrfHeader) {
      headers[csrfHeader] = csrfToken;
    }

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
      memoCounter:  document.getElementById('memo-counter'),
      submit:       document.getElementById('submitBtn'),
    };
  }

  async function fetchJson(url, init = {}) {
    const headers = buildSecureHeaders(init.method !== 'GET');

    try {
      const res = await fetch(url, {
        credentials: 'same-origin',
        ...init,
        headers: { ...headers, ...init.headers }
      });

      if (res.status === 403) {
        console.error('CSRF 토큰 오류 또는 권한 없음');
        alert('보안 토큰이 만료되었습니다. 페이지를 새로고침 해주세요.');
        location.reload();
        return null;
      }

      const ct = res.headers.get('content-type') || '';
      if (ct.includes('text/html')) {
        throw new Error('AUTH_REDIRECT');
      }

      if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
          msg = await res.text();
        } catch {}
        throw new Error(msg);
      }

      const text = await res.text().catch(() => '');
      try {
        return text ? JSON.parse(text) : null;
      } catch {
        return null;
      }
    } catch (error) {
      if (error.message === 'AUTH_REDIRECT') {
        window.location.href = '/user/login';
        return null;
      }
      throw error;
    }
  }

  async function fetchResultById(resultId)  {
    return fetchJson(API_BY_ID(resultId));
  }

  async function fetchLatestResult() {
    return fetchJson(API_LATEST);
  }

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

    const targetId = data.resultId ?? data.id;
    if (el.resultId && !isBlank(targetId)) {
      const opt = el.resultId.querySelector(`option[value="${String(targetId)}"]`);
      if (opt) {
        el.resultId.value = String(targetId);
      } else {
        console.warn('URL/프리필 ID가 내 목록에 없어 선택하지 않습니다:', targetId);
      }
    }
  }

  (async function bootPrefill() {
    try {
      initCsrfToken();

      const params = new URLSearchParams(location.search);
      const rid = params.get('interviewResultId');

      let data = null;
      if (rid && !isBlank(rid)) {
        data = await fetchResultById(rid).catch(() => null);
      } else {
        data = await fetchLatestResult().catch(() => null);
      }
      fillFormFromResult(data);
    } catch (e) {
      console.error('프리필 오류:', e);
    }
  })();

  document.addEventListener('change', function(e) {
    const el = $els();
    if (e.target && e.target.id === 'interviewResultId') {
      if (el.title && isBlank(el.title.value)) {
        const selectedText = el.resultId.options[el.resultId.selectedIndex]?.text || '';
        if (!isBlank(selectedText)) {
          el.title.value = `[면접결과] ${selectedText}`;
        }
      }
    }
  });

  function buildPayload() {
    const el = $els();
    const interviewResultId =
      el.resultId && !isBlank(el.resultId.value) ?
        toNum(el.resultId.value) : null;
    const title = el.title?.value?.trim() || '';
    const content = el.memo?.value?.trim() || '';

    if (isBlank(title)) throw new Error('제목을 입력해주세요.');
    if (interviewResultId == null || Number.isNaN(interviewResultId)) {
      throw new Error('면접 결과를 선택해주세요.');
    }

    return {
      category: 'INTERVIEW_SHARE',
      interviewShare: {
        interviewResultId,
        title,
        content
      }
    };
  }

  document.addEventListener('DOMContentLoaded', function() {
    const el = $els();

    if (el.memo) {
      el.memo.addEventListener('input', () => updateMemoCounter(el));
      updateMemoCounter(el);
    }

    if (el.form && el.resultId) {
      el.form.addEventListener('submit', function(e) {
        if (isBlank(el.resultId.value)) {
          e.preventDefault();
          alert('면접 결과를 선택해주세요.');
        }
      }, { capture: true });
    }

  });
})();
