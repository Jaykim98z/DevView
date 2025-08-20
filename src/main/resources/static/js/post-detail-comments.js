'use strict';

function __getCsrf__() {
  const t = document.querySelector('meta[name="_csrf"]')?.content;
  const h = document.querySelector('meta[name="_csrf_header"]')?.content;
  if (t && h) return { header: h, token: t };
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  if (m) return { header: 'X-XSRF-TOKEN', token: decodeURIComponent(m[1]) };
  return { header: null, token: null };
}
function __secureHeaders__(json = true, baseHeaders = {}) {
  const { header, token } = __getCsrf__();
  const h = { 'X-Requested-With': 'XMLHttpRequest', ...baseHeaders };
  if (json) h['Content-Type'] = 'application/json';
  if (header && token) h[header] = token;
  return h;
}
async function fetchJson(url, opts = {}) {
  const hasBody = !!opts.body || !!opts.bodyObj;
  const headers = __secureHeaders__(hasBody, opts.headers || {});
  const res = await fetch(url, {
    method: opts.method || 'GET',
    headers,
    credentials: opts.credentials || 'same-origin',
    body: opts.bodyObj ? JSON.stringify(opts.bodyObj) : opts.body
  });
  if (res.status === 401) throw new Error('UNAUTHORIZED');
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try { msg = await res.text(); } catch {}
    throw new Error(msg || 'REQUEST_FAILED');
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) return res.json();
  return {};
}

function getPostId() {
  const fromWindow = window.POST_ID;
  const fromBody = document.body.dataset.postId;
  return fromWindow ?? (fromBody ? Number(fromBody) : null);
}
function toggleIconSolid(iconEl, makeSolid) {
  if (!iconEl) return;
  iconEl.classList.toggle('fa-solid', !!makeSolid);
  iconEl.classList.toggle('fa-regular', !makeSolid);
}

function buildActionsHTML() {
  return `
    <button id="btn-edit-post"  class="btn-action">수정</button>
    <button id="btn-delete-post" class="btn-action btn-danger">삭제</button>
  `;
}
function buildEditActionsHTML() {
  return `
    <button id="btn-save-post"   class="btn-submit">저장</button>
    <button id="btn-cancel-edit" class="btn-action">취소</button>
  `;
}

function initLikeScrap() {
  const postId = getPostId();
  if (!postId) return;

  const likeAddUrl  = `/api/community/posts/${postId}/likes`;
  const likeDelUrl  = `/api/community/posts/${postId}/likes`;
  const scrapAddUrl = `/api/community/posts/${postId}/scraps`;
  const scrapDelUrl = `/api/community/posts/${postId}/scraps`;

  document.querySelectorAll('.btn-like').forEach(btn => {
    btn.addEventListener('click', async () => {
      const willActivate = !btn.classList.contains('is-active');
      const url   = willActivate ? likeAddUrl : likeDelUrl;
      const method = willActivate ? 'POST' : 'DELETE';

      try {
        const data = await fetchJson(url, { method });
        const active = data?.active ?? willActivate;

        btn.classList.toggle('is-active', active);
        btn.setAttribute('aria-pressed', String(active));
        toggleIconSolid(btn.querySelector('i'), active);

        const countEl = btn.querySelector('.count');
        if (countEl && typeof data?.count === 'number') countEl.textContent = String(data.count);
      } catch (e) {
        if (e.message === 'UNAUTHORIZED') alert('로그인이 필요합니다.');
        else alert('좋아요 처리에 실패했습니다.');
      }
    });
  });

  document.querySelectorAll('.btn-scrap').forEach(btn => {
    btn.addEventListener('click', async () => {
      const willActivate = !btn.classList.contains('is-active');
      const url   = willActivate ? scrapAddUrl : scrapDelUrl;
      const method = willActivate ? 'POST' : 'DELETE';

      try {
        const data = await fetchJson(url, { method });
        const active = data?.active ?? willActivate;

        btn.classList.toggle('is-active', active);
        btn.setAttribute('aria-pressed', String(active));
        toggleIconSolid(btn.querySelector('i'), active);

        const countEl = btn.querySelector('.count');
        if (countEl && typeof data?.count === 'number') countEl.textContent = String(data.count);
      } catch (e) {
        if (e.message === 'UNAUTHORIZED') alert('로그인이 필요합니다.');
        else alert('스크랩 처리에 실패했습니다.');
      }
    });
  });
}

function enterEditMode() {
  if (document.querySelector('.title-input') || document.querySelector('.content-input')) {
    document.querySelector('.title-input, .content-input')?.focus();
    return null;
  }

  const titleEl   = document.querySelector('.post-title');
  const contentEl = document.querySelector('.post-content');
  const actions   = document.querySelector('.post-actions');
  if (!titleEl || !contentEl || !actions) return null;

  const original = {
    title:   titleEl.textContent.trim(),
    content: contentEl.textContent.trim(),
    actionsHTML: actions.innerHTML
  };

  const titleInput = document.createElement('input');
  titleInput.type = 'text';
  titleInput.className = 'title-input';
  titleInput.value = original.title;
  Object.assign(titleInput.style, {
    width: '100%', fontSize: '20px', fontWeight: '700',
    padding: '8px 10px', border: '1px solid #d7e0ea', borderRadius: '10px'
  });

  const textarea = document.createElement('textarea');
  textarea.className = 'content-input';
  textarea.value = original.content;
  Object.assign(textarea.style, {
    width: '100%', minHeight: '220px',
    padding: '12px', border: '1px solid #d7e0ea', borderRadius: '12px',
    fontSize: '15px', lineHeight: '1.8'
  });

  titleEl.replaceWith(titleInput);
  contentEl.replaceWith(textarea);

  actions.innerHTML = buildEditActionsHTML();

  const restoreView = (title, content) => {
    const newTitle = document.createElement('h2');
    newTitle.className = 'post-title';
    newTitle.textContent = title;

    const newContent = document.createElement('div');
    newContent.className = 'post-content';
    newContent.textContent = content;

    titleInput.replaceWith(newTitle);
    textarea.replaceWith(newContent);

    actions.innerHTML = buildActionsHTML();
    initEditDelete();
  };

  return { titleInput, textarea, original, restoreView };
}

async function tryUpdatePost(postId, payload) {

  const url = `/api/community/posts/${postId}`;
  try {
    return await fetchJson(url, {
      method: 'PATCH',
      bodyObj: { title: payload.title, content: payload.content }
    });
  } catch {
    return await fetchJson(url, {
      method: 'PATCH',
      bodyObj: { title: payload.title, summary: payload.content }
    });
  }
}

async function tryDeletePost(postId) {
  await fetchJson(`/api/community/posts/${postId}`, { method: 'DELETE' });
}

function initEditDelete() {
  const editBtn   = document.getElementById('btn-edit-post');
  const deleteBtn = document.getElementById('btn-delete-post');

  if (editBtn) {
    editBtn.addEventListener('click', () => {
      if (!confirm('수정하시겠습니까?')) return;

      const ctx = enterEditMode();
      if (!ctx) return;

      const { titleInput, textarea, original, restoreView } = ctx;
      const postId = getPostId();

      const saveBtn   = document.getElementById('btn-save-post');
      const cancelBtn = document.getElementById('btn-cancel-edit');

      saveBtn.addEventListener('click', async () => {
        const title = titleInput.value.trim();
        const content = textarea.value.trim();
        if (!title || !content) { alert('제목과 내용을 입력해주세요.'); return; }

        try {
          await tryUpdatePost(postId, { title, content });
          alert('수정되었습니다.');
          restoreView(title, content);
        } catch (e) {
          console.error(e);
          alert('수정에 실패했습니다.');
        }
      });

      cancelBtn.addEventListener('click', () => {
        restoreView(original.title, original.content);
      });
    });
  }

  if (deleteBtn) {
    deleteBtn.addEventListener('click', async () => {
      if (!confirm('삭제하시겠습니까?')) return;
      const postId = getPostId();
      try {
        await tryDeletePost(postId);
        alert('삭제되었습니다.');
        window.location.href = '/community';
      } catch (e) {
        console.error(e);
        alert('삭제에 실패했습니다.');
      }
    });
  }
}

(function () {
  const start = () => {
    initLikeScrap();
    initEditDelete();
  };
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start, { once: true });
  } else {
    start();
  }
})();
