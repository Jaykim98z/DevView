(function () {
  const API_LATEST = '/api/community/interview-results/latest';
  const API_BY_ID  = (id) => `/api/community/interview-results/${encodeURIComponent(id)}`;
  const API_COMPOSE = '/api/community/posts/compose';

  const isBlank = (v) => v == null || String(v).trim() === '';
  const toNum   = (v) => Number(v ?? NaN);

  // ✅ CSRF 토큰 초기화
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

  // ✅ 보안 헤더를 포함한 fetch 함수
  function buildSecureHeaders(includeContentType = true) {
    const headers = {
      'X-Requested-With': 'XMLHttpRequest',
      'Accept': 'application/json'
    };

    if (includeContentType) {
      headers['Content-Type'] = 'application/json';
    }

    // CSRF 토큰 헤더 추가
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
      // 페이지 로드 시 CSRF 토큰 초기화
      initCsrfToken();

      const params = new URLSearchParams(location.search);
      const rid = params.get('interviewResultId');
      let data = null;
      if (rid && !isBlank(rid)) data = await fetchResultById(rid);
      else data = await fetchLatestResult();
      fillFormFromResult(data);
    } catch (e) {
      console.error('프리필 오류:', e);
    }
  })();

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

    return { interviewResultId, title, content };
  }

  document.addEventListener('DOMContentLoaded', function() {
    const el = $els();

    // 글자 수 카운터
    if (el.memo) {
      el.memo.addEventListener('input', () => updateMemoCounter(el));
      updateMemoCounter(el);
    }

    // 폼 제출
    if (el.form) {
      el.form.addEventListener('submit', async function(e) {
        e.preventDefault();

        try {
          const payload = buildPayload();

          if (el.submit) el.submit.disabled = true;

          // ✅ 보안 헤더를 포함한 글 작성 요청
          const result = await fetchJson(API_COMPOSE, {
            method: 'POST',
            body: JSON.stringify(payload)
          });

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
          if (el.submit) el.submit.disabled = false;
        }
      });
    }
  });
})();