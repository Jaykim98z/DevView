(function () {
  const FETCH_URL = '/api/v1/interviews/results/latest';

  const form =
    document.querySelector('form[th\\:action], form[action*="/community/posts/interview"]') ||
    document.querySelector('form');
  if (!form) return;

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

  if (!el.resultId) {
    const hidden = document.createElement('input');
    hidden.type = 'hidden';
    hidden.id = 'interviewResultId';
    hidden.name = 'interviewResultId';
    form.appendChild(hidden);
    el.resultId = hidden;
  }

  const isBlank = (v) => !v || !String(v).trim();
  const toNum = (v) => Number(v ?? NaN);

  async function fetchResultById(resultId) {
    const res = await fetch(`/api/v1/interviews/results/${encodeURIComponent(resultId)}`, {
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
      credentials: 'same-origin',
    });
    if (!res.ok) throw new Error('결과 조회 실패');
    return res.json();
  }

  async function fetchLatestResult() {
    const alreadyFilled =
      el.resultId && !isBlank(el.resultId.value) &&
      el.grade && !isBlank(el.grade.value) &&
      el.score && !isBlank(el.score.value) &&
      el.feedback && !isBlank(el.feedback.value);
    if (alreadyFilled) return null;

    const controller = new AbortController();
    const t = setTimeout(() => controller.abort(), 5000);
    try {
      const res = await fetch(FETCH_URL, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        credentials: 'same-origin',
        signal: controller.signal,
      });
      clearTimeout(t);

      if (res.status === 204) return null;
      if (!res.ok) throw new Error('최신 결과 조회 실패');
      return res.json();
    } catch (e) {
      clearTimeout(t);
      return null;
    }
  }

  function fillFormFromResult(d) {
    if (!d) return;
    if (el.resultId && isBlank(el.resultId.value) && d.resultId != null) el.resultId.value = d.resultId;
    if (el.grade && d.grade != null) {
      const val = String(d.grade);
      const opt = el.grade.querySelector(`option[value="${val}"]`);
      if (opt) el.grade.value = val;
    }
    if (el.score && d.totalScore != null) {
      const n = Number(d.totalScore);
      if (!Number.isNaN(n)) el.score.value = Math.max(0, Math.min(100, n));
    }
    if (el.feedback && isBlank(el.feedback.value) && d.feedback != null) el.feedback.value = d.feedback;
    if (el.title && isBlank(el.title.value)) {
      const g = d.grade ?? '';
      const s = d.totalScore ?? '';
      el.title.value = `[면접결과] ${g}${g ? ' ' : ''}${s !== '' ? `${s}점` : ''}`.trim();
    }
    if (el.memo && isBlank(el.memo.value) && d.recommendedResource) {
      el.memo.value = `추천 자료: ${d.recommendedResource}`;
      updateMemoCounter();
    }
  }

  async function fetchMyResults(page = 0, size = 10) {
    const res = await fetch(`/api/v1/interviews/my-results?page=${page}&size=${size}`, {
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
      credentials: 'same-origin',
    });
    if (!res.ok) throw new Error('목록 조회 실패');
    return res.json();
  }

  function ensurePickerUI() {
    let btn = document.getElementById('openResultPicker');
    if (!btn) {
      btn = document.createElement('button');
      btn.type = 'button';
      btn.id = 'openResultPicker';
      btn.textContent = '결과 선택';
      const target = el.title || form.querySelector('h1, h2') || form;
      target.parentNode.insertBefore(btn, target.nextSibling);
    }

    let dlg = document.getElementById('resultPickerDialog');
    if (!dlg) {
      dlg = document.createElement('dialog');
      dlg.id = 'resultPickerDialog';
      const wrapper = document.createElement('div');
      const header = document.createElement('div');
      const title = document.createElement('strong');
      title.textContent = '인터뷰 결과 선택';
      header.appendChild(title);
      const list = document.createElement('ul');
      list.id = 'resultPickerList';
      list.setAttribute('style', 'list-style:none;padding:0;margin:12px 0;max-height:50vh;overflow:auto;');
      const footer = document.createElement('div');
      const prev = document.createElement('button');
      prev.type = 'button';
      prev.id = 'pickerPrev';
      prev.textContent = '이전';
      const next = document.createElement('button');
      next.type = 'button';
      next.id = 'pickerNext';
      next.textContent = '다음';
      const close = document.createElement('button');
      close.type = 'button';
      close.id = 'pickerClose';
      close.textContent = '닫기';
      footer.appendChild(prev);
      footer.appendChild(next);
      footer.appendChild(close);
      wrapper.appendChild(header);
      wrapper.appendChild(list);
      wrapper.appendChild(footer);
      dlg.appendChild(wrapper);
      document.body.appendChild(dlg);

      close.addEventListener('click', () => dlg.close());
      if (!dlg.showModal) {
        dlg.setAttribute('role', 'dialog');
        dlg.setAttribute('aria-modal', 'true');
        dlg.open = false;
        dlg.showModal = function () { this.open = true; this.style.display = 'block'; };
        dlg.close = function () { this.open = false; this.style.display = 'none'; };
      }

      let page = 0;
      const size = 10;

      async function render() {
        const data = await fetchMyResults(page, size);
        const content = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : []);
        list.innerHTML = '';
        content.forEach(item => {
          const li = document.createElement('li');
          const btnSel = document.createElement('button');
          const created = item.createdAt || '';
          const jt = item.jobPosition || '';
          const it = item.interviewType || '';
          const sc = item.totalScore != null ? item.totalScore : '';
          const gr = item.grade || '';
          const rid = item.resultId || item.id || '';
          btnSel.type = 'button';
          btnSel.textContent = `#${rid} ${created} ${jt} ${it} ${gr} ${sc}`;
          btnSel.addEventListener('click', async () => {
            if (rid) {
              const data = await fetchResultById(rid);
              if (el.resultId) el.resultId.value = rid;
              fillFormFromResult(data);
              dlg.close();
            }
          });
          li.appendChild(btnSel);
          list.appendChild(li);
        });
        if (data.totalPages != null) {
          prev.disabled = page <= 0;
          next.disabled = page >= data.totalPages - 1;
        }
      }

      prev.addEventListener('click', async () => { page = Math.max(0, page - 1); await render(); });
      next.addEventListener('click', async () => { page = page + 1; await render(); });

      btn.addEventListener('click', async () => { await render(); dlg.showModal(); });
    }

    return btn;
  }

  (async function loadOnEnter() {
    const params = new URLSearchParams(location.search);
    const rid = params.get('interviewResultId');
    if (rid) {
      if (el.resultId && isBlank(el.resultId.value)) el.resultId.value = rid;
      try {
        const data = await fetchResultById(rid);
        fillFormFromResult(data);
      } catch (e) {}
    } else {
      const latest = await fetchLatestResult();
      fillFormFromResult(latest);
    }
    ensurePickerUI();
  })();

  el.resultId?.addEventListener('blur', async () => {
    const id = el.resultId.value;
    if (isBlank(id)) return;
    try {
      const data = await fetchResultById(id);
      fillFormFromResult(data);
    } catch (e) {}
  });

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

  function lockSubmit() { if (!el.submit) return; el.submit.disabled = true; el.submit.textContent = '등록 중...'; }

  form.addEventListener('submit', (e) => {
    if (el.submit?.disabled) { e.preventDefault(); return; }
    if (!validate()) { e.preventDefault(); return; }
    lockSubmit();
  });

  el.title?.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') ev.preventDefault(); });
  (function focusFirst() { (el.title || el.memo)?.focus({ preventScroll: true }); })();
})();
