'use strict';

function escapeHtml(s) {
  if (s == null) return '';
  return String(s)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function __getCsrf__() {
  const t = document.querySelector('meta[name="_csrf"]')?.content;
  const h = document.querySelector('meta[name="_csrf_header"]')?.content;
  if (t && h) return { header: h, token: t };
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  if (m) return { header: 'X-XSRF-TOKEN', token: decodeURIComponent(m[1]) };
  return { header: null, token: null };
}
function __secureHeaders__(json = true) {
  const { header, token } = __getCsrf__();
  const h = { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'application/json' };
  if (json) h['Content-Type'] = 'application/json';
  if (header && token) h[header] = token;
  return h;
}
async function fetchJson(url, opts = {}) {
  if (!('credentials' in opts)) opts.credentials = 'same-origin';
  const res = await fetch(url, opts);
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('text/html')) {
    throw new Error('AUTH_REDIRECT');
  }
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try { msg = await res.text(); } catch {}
    throw new Error(msg);
  }
  if (ct.includes('application/json')) return res.json();
  return {};
}

function getPostId() {
  const card = document.querySelector('.post-detail-card');
  const fromCard = card?.dataset.postId;
  if (fromCard && fromCard !== 'null' && fromCard !== 'undefined') return Number(fromCard);
  const fromWindow = window.POST_ID;
  if (fromWindow != null && !Number.isNaN(Number(fromWindow))) return Number(fromWindow);
  const fromBody = document.body.dataset.postId;
  if (fromBody && fromBody !== 'null' && fromBody !== 'undefined') return Number(fromBody);
  return null;
}

function readMeta(name) {
  return document.querySelector(`meta[name="${name}"]`)?.content || '';
}

function getPostWriterId() {
  const card = document.querySelector('.post-detail-card');
  const fromCard = card?.dataset.writerId;
  if (fromCard && fromCard !== 'null' && fromCard !== 'undefined' && !Number.isNaN(Number(fromCard))) return Number(fromCard);
  const fromBody = document.body?.dataset?.writerId;
  if (fromBody && fromBody !== 'null' && fromBody !== 'undefined' && !Number.isNaN(Number(fromBody))) return Number(fromBody);
  const fromWindow = window.POST_WRITER_ID;
  if (fromWindow != null && !Number.isNaN(Number(fromWindow))) return Number(fromWindow);
  const fromMeta = readMeta('post-writer-id');
  if (fromMeta && !Number.isNaN(Number(fromMeta))) return Number(fromMeta);
  return null;
}

function emailLocal(s) {
  return (typeof s === 'string' && s.includes('@')) ? s.split('@')[0] : null;
}
function normName(s) {
  return emailLocal(s) ?? (s != null ? String(s) : null);
}

function getCurrentUser() {
  const b = document.body?.dataset || {};
  const id = b.currentUserId ?? window.CURRENT_USER_ID;
  const username = b.currentUsername ?? window.CURRENT_USERNAME;
  const email = b.currentUserEmail ?? window.CURRENT_USER_EMAIL;
  const usernameNorm = normName(username);
  const emailNorm = normName(email);
  return {
    id: id != null && id !== 'null' && id !== 'undefined' ? Number(id) : null,
    username,
    email,
    usernameNorm,
    emailNorm
  };
}

function getCurrentUserFromMeta() {
  const id = readMeta('current-user-id');
  const username = readMeta('current-username');
  const email = readMeta('current-user-email');
  return {
    id: id && id !== 'null' && id !== 'undefined' ? String(id) : '',
    username: username || '',
    email: email || ''
  };
}
async function ensureCurrentUserGlobals() {
  if (window.CURRENT_USER_ID && window.CURRENT_USERNAME) return;
  const b = document.body?.dataset || {};
  if ((b.currentUserId && b.currentUserId !== 'null' && b.currentUserId !== 'undefined') || b.currentUsername) {
    window.CURRENT_USER_ID = b.currentUserId ? String(b.currentUserId) : window.CURRENT_USER_ID;
    window.CURRENT_USERNAME = b.currentUsername ? String(b.currentUsername) : window.CURRENT_USERNAME;
    window.CURRENT_USER_EMAIL = b.currentUserEmail ? String(b.currentUserEmail) : window.CURRENT_USER_EMAIL;
    return;
  }
  const meta = getCurrentUserFromMeta();
  if (meta.id || meta.username || meta.email) {
    window.CURRENT_USER_ID = meta.id || window.CURRENT_USER_ID || '';
    window.CURRENT_USERNAME = meta.username || window.CURRENT_USERNAME || '';
    window.CURRENT_USER_EMAIL = meta.email || window.CURRENT_USER_EMAIL || '';
    return;
  }
  try {
    const me = await fetchJson('/api/auth/me', { headers: __secureHeaders__(false) });
    if (me && typeof me === 'object') {
      if (me.userId != null) window.CURRENT_USER_ID = String(me.userId);
      if (me.username) window.CURRENT_USERNAME = String(me.username);
      if (me.email) window.CURRENT_USER_EMAIL = String(me.email);
    }
  } catch {}
}

function isOwnerById(ownerId) {
  const me = getCurrentUser();
  if (ownerId == null || me.id == null) return false;
  return Number(ownerId) === Number(me.id);
}

function toggleIconSolid(iconEl, makeSolid) {
  if (!iconEl) return;
  iconEl.classList.toggle('fa-solid', !!makeSolid);
  iconEl.classList.toggle('fa-regular', !makeSolid);
}
function initLikeScrap() {
  document.querySelectorAll('.btn-like').forEach(btn => {
    btn.addEventListener('click', async () => {
      const url = btn.dataset.url || `/api/community/posts/${getPostId()}/like`;
      try {
        const data = await fetchJson(url, { method: 'POST', headers: __secureHeaders__(), body: JSON.stringify({}) });
        const active = !!data.active;
        btn.classList.toggle('is-active', active);
        toggleIconSolid(btn.querySelector('i'), active);
        const countEl = btn.querySelector('.count');
        if (typeof data.count === 'number' && countEl) countEl.textContent = String(data.count);
      } catch (e) {
        if (String(e.message).startsWith('AUTH_REDIRECT')) { alert('로그인이 필요합니다.'); location.href = '/login'; return; }
        alert('좋아요 처리에 실패했습니다.');
      }
    });
  });
  document.querySelectorAll('.btn-scrap').forEach(btn => {
    btn.addEventListener('click', async () => {
      const url = btn.dataset.url || `/api/community/posts/${getPostId()}/scrap`;
      try {
        const data = await fetchJson(url, { method: 'POST', headers: __secureHeaders__(), body: JSON.stringify({}) });
        const active = !!data.active;
        btn.classList.toggle('is-active', active);
        toggleIconSolid(btn.querySelector('i'), active);
        const countEl = btn.querySelector('.count');
        if (typeof data.count === 'number' && countEl) countEl.textContent = String(data.count);
      } catch (e) {
        if (String(e.message).startsWith('AUTH_REDIRECT')) { alert('로그인이 필요합니다.'); location.href = '/login'; return; }
        alert('스크랩 처리에 실패했습니다.');
      }
    });
  });
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

function ensureOwnerPostButtons() {
  const actions = document.querySelector('.post-actions');
  if (!actions) return;
  const writerId = getPostWriterId();
  const owner = isOwnerById(writerId);
  const hasEdit = actions.querySelector('#btn-edit-post');
  const hasDel = actions.querySelector('#btn-delete-post');
  if (owner) {
    if (!hasEdit && !hasDel) {
      actions.insertAdjacentHTML('afterbegin', buildActionsHTML());
    }
  } else {
    if (hasEdit) hasEdit.remove();
    if (hasDel) hasDel.remove();
  }
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

  const textarea = document.createElement('textarea');
  textarea.className = 'content-input';
  textarea.value = original.content;

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
  const headers = __secureHeaders__();
  const bodies = [
    JSON.stringify({ title: payload.title, content: payload.content }),
    JSON.stringify({ title: payload.title, summary: payload.content }),
  ];
  for (const body of bodies) {
    try {
      return await fetchJson(`/api/community/posts/${postId}`, { method: 'PATCH', headers, body });
    } catch {}
  }
  return await fetchJson(`/api/community/posts/${postId}`, {
    method: 'PUT', headers, body: JSON.stringify({ id: postId, title: payload.title, summary: payload.content })
  });
}
async function tryDeletePost(postId) {
  const headers = __secureHeaders__();
  try { await fetchJson(`/api/community/posts/${postId}/soft`, { method: 'DELETE', headers }); return; }
  catch {}
  await fetchJson(`/api/community/posts/${postId}`, { method: 'DELETE', headers });
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
        if (!title) { alert('제목을 입력해주세요.'); return; }

        try {
          await tryUpdatePost(postId, { title, content });
          alert('수정되었습니다.');
          restoreView(title, content);
        } catch (e) {
          if (String(e.message).startsWith('AUTH_REDIRECT')) { alert('로그인이 필요합니다.'); location.href = '/login'; return; }
          alert('수정에 실패했습니다.');
        }
      });

      cancelBtn.addEventListener('click', () => restoreView(original.title, original.content));
    });
  }

  if (deleteBtn) {
    deleteBtn.addEventListener('click', async () => {
      if (!confirm('삭제하시겠습니까?')) return;
      const postId = getPostId();
      try {
        await fetchJson(`/api/community/posts/${postId}/comments`, { method: 'GET', headers: __secureHeaders__(false) });
        await tryDeletePost(postId);
        alert('삭제되었습니다.');
        location.href = '/community';
      } catch (e) {
        if (String(e.message).startsWith('AUTH_REDIRECT')) { alert('로그인이 필요합니다.'); location.href = '/login'; return; }
        alert('삭제에 실패했습니다.');
      }
    });
  }
}

const CMT = {
  page: 0,
  size: 10,
  last: false,
  loading: false,
};

function formatKoreanDate(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return String(iso);
  return d
    .toLocaleString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: '2-digit' })
    .replace(/\s*\n+\s*/g, ' ')
    .trim();
}

function commentCountEl() {
  return document.getElementById('comment-count');
}

function buildCommentItem(c) {
  const rawAuthor = c.username ?? c.writerName ?? c.author ?? '익명';
  const displayAuthor = (typeof rawAuthor === 'string' && rawAuthor.includes('@'))
   ? rawAuthor.split('@')[0]
   : rawAuthor;

  const content  = c.content ?? c.text ?? '';
  const created  = c.createdAt ?? c.created_at ?? c.createdAtIso ?? null;
  const uid      = c.userId ?? c.writerId ?? c.authorId ?? null;
  const id = (c.id ?? c.commentId ?? c.commentID ?? c.comment_id ?? c['comment-id'] ?? null);

  const me = getCurrentUser();
  const authorNorm = normName(rawAuthor);
  const isOwnerFallback =
    (
      (uid != null && me.id != null && Number(uid) === Number(me.id)) ||
      (authorNorm != null && me.usernameNorm != null && authorNorm === me.usernameNorm) ||
      (authorNorm != null && me.emailNorm    != null && authorNorm === me.emailNorm)
    );

  const canEdit = !!(c.canEdit ?? c.mine ?? c.ownedByMe ?? isOwnerFallback);
  const canDelete = !!(c.canDelete ?? c.mine ?? c.ownedByMe ?? isOwnerFallback);
  const showActions = canEdit || canDelete;

  const li = document.createElement('li');
  li.className = 'comment-item';
  if (id != null) li.dataset.commentId = String(id);
  if (showActions) li.classList.add('is-owner');

  li.innerHTML = `
    <img class="comment-item__avatar" src="/img/profile-default.svg" alt="" />
    <div>
      <div class="comment-item__head">
        <span class="comment-item__author">${escapeHtml(displayAuthor)}</span>
        <span class="comment-item__meta">${escapeHtml(formatKoreanDate(created))}</span>
      </div>
      <div class="comment-item__body comment__content"></div>
      <div class="comment-item__actions comment__actions ${showActions ? '' : 'is-hidden'}">
        <button class="comment-action btn-action comment__edit" type="button">수정</button>
        <button class="comment-action comment-action--danger btn-action btn-danger comment__delete" type="button">삭제</button>
      </div>
    </div>
  `;

  li.querySelector('.comment-item__body').textContent = content;

  if (showActions) {
    li.querySelector('.comment__delete')?.addEventListener('click', () => onDeleteComment(id, li));
    li.querySelector('.comment__edit')?.addEventListener('click', () => onEditComment(id, li, content));
  } else {
    li.querySelector('.comment-item__actions')?.classList.add('is-hidden');
  }

  return li;
}

function updateCommentCount(delta) {
  const el = commentCountEl();
  if (!el) return;
  const curr = Number(el.textContent || '0') || 0;
  el.textContent = String(curr + delta);
}

async function loadComments(reset = false) {
  if (CMT.loading || CMT.last && !reset) return;
  const postId = getPostId();
  if (postId == null) return;

  CMT.loading = true;
  try {
    const page = reset ? 0 : CMT.page;
    const data = await fetchJson(`/api/community/posts/${postId}/comments?page=${page}&size=${CMT.size}`, {
      headers: __secureHeaders__(false)
    });

    const list = document.getElementById('comment-list');
    if (reset) list.innerHTML = '';

    const items = Array.isArray(data?.content) ? data.content : [];
    for (const c of items) list.appendChild(buildCommentItem(c));

    CMT.last = !!data?.last;
    CMT.page = page + 1;

    const moreBtn = document.getElementById('comment-load-more');
    if (moreBtn) moreBtn.classList.toggle('is-hidden', CMT.last);

    if (reset && typeof data?.totalElements === 'number') {
      const cc = commentCountEl();
      if (cc) cc.textContent = String(data.totalElements);
    }
  } catch (e) {
    if (String(e.message).startsWith('AUTH_REDIRECT')) { }
    console.error('댓글 로딩 실패', e);
  } finally {
    CMT.loading = false;
  }
}

async function onDeleteComment(commentId, liEl) {
  if (!commentId) return;
  if (!confirm('댓글을 삭제하시겠습니까?')) return;
  const postId = getPostId();
  try {
    await fetchJson(`/api/community/posts/${postId}/comments/${commentId}`, {
      method: 'DELETE',
      headers: __secureHeaders__(false)
    });
    liEl?.remove();
    updateCommentCount(-1);
  } catch (e) {
    if (String(e.message).startsWith('AUTH_REDIRECT')) { alert('로그인이 필요합니다.'); location.href = '/login'; return; }
    alert('댓글 삭제에 실패했습니다.');
  }
}

function onEditComment(commentId, liEl, oldContent) {
  const contentEl = liEl.querySelector('.comment__content');
  const actionsEl = liEl.querySelector('.comment__actions');
  if (!contentEl || !actionsEl) return;

  const ta = document.createElement('textarea');
  ta.className = 'comment__edit-input';
  ta.value = oldContent ?? contentEl.textContent ?? '';

  const ctrl = document.createElement('div');
  ctrl.className = 'comment__edit-controls';
  ctrl.innerHTML = `
    <button class="comment__save btn-submit" type="button">저장</button>
    <button class="comment__cancel btn-action" type="button">취소</button>
  `;

  const origText = contentEl.textContent;
  contentEl.replaceChildren(ta);
  actionsEl.classList.add('is-hidden');
  contentEl.after(ctrl);

  const cleanup = () => {
    contentEl.textContent = ta.value.trim() || origText || '';
    ctrl.remove();
    actionsEl.classList.remove('is-hidden');
  };

  ctrl.querySelector('.comment__cancel').addEventListener('click', () => {
    ta.value = origText;
    cleanup();
  });

  ctrl.querySelector('.comment__save').addEventListener('click', async () => {
    const newText = ta.value.trim();
    if (!newText) { alert('내용을 입력하세요.'); return; }

    const postId = getPostId();
    try {
      await fetchJson(`/api/community/posts/${postId}/comments/${commentId}`, {
        method: 'PUT',
        headers: __secureHeaders__(true),
        body: JSON.stringify({ content: newText })
      });
      ta.value = newText;
      cleanup();
    } catch (e) {
      if (String(e.message).startsWith('AUTH_REDIRECT')) { alert('로그인이 필요합니다.'); location.href = '/login'; return; }
      alert('댓글 수정에 실패했습니다.');
    }
  });
}

function initComments() {
  const btn = document.getElementById('comment-submit');
  const input = document.getElementById('comment-input');
  const moreBtn = document.getElementById('comment-load-more');
  if (!btn || !input) return;

  CMT.page = 0; CMT.last = false;
  loadComments(true);

  moreBtn?.addEventListener('click', () => loadComments(false));

  btn.addEventListener('click', async () => {
    const content = (input.value || '').trim();
    if (!content) return;

    const postId = getPostId();
    const url = `/api/community/posts/${postId}/comments`;

    try {
      const created = await fetchJson(url, {
        method: 'POST',
        headers: __secureHeaders__(true),
        body: JSON.stringify({ content })
      });

      input.value = '';
      const ul = document.getElementById('comment-list');

      const me = getCurrentUser();
      if (created && typeof created === 'object') {
        created.mine = true;
        created.canEdit = true;
        created.canDelete = true;
        if (me.id != null) created.userId = me.id;
        if (!created.username && me.username) created.username = me.username;
        if (!created.createdAt) created.createdAt = new Date().toISOString();
      }
      const li = buildCommentItem(created ?? {
        id: undefined,
        content,
        username: (me.username ?? me.usernameNorm ?? '익명'),
        userId: me.id ?? null,
        createdAt: new Date().toISOString(),
        mine: true,
        canEdit: true,
        canDelete: true
      });

      ul.prepend(li);
      updateCommentCount(1);
    } catch (e) {
      if (String(e.message).startsWith('AUTH_REDIRECT')) {
        alert('로그인이 필요합니다.');
        location.href = '/login';
        return;
      }
      alert('댓글 등록에 실패했습니다.');
    }
  });
}

async function bumpViewCount() {
  const postId = getPostId();
  if (postId == null) return;
  try {
    const res = await fetchJson(`/api/community/posts/${postId}/view`, {
      method: 'POST',
      headers: __secureHeaders__(true),
      body: JSON.stringify({})
    });
    if (res && typeof res.viewCount === 'number') {
      const vc = document.getElementById('view-count');
      if (vc) vc.textContent = String(res.viewCount);
    } else {
      const vc = document.getElementById('view-count');
      if (vc) vc.textContent = String((Number(vc.textContent || '0') || 0) + 1);
    }
  } catch (e) {
    console.warn('조회수 증가 실패', e);
  }
}

(function () {
  const start = async () => {
    await ensureCurrentUserGlobals();
    ensureOwnerPostButtons();
    initLikeScrap();
    initEditDelete();
    initComments();
    bumpViewCount();
  };
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start, { once: true });
  } else {
    start();
  }
  window.addEventListener('pageshow', (e) => {
    if (e.persisted) {
      CMT.page = 0;
      CMT.last = false;
      loadComments(true);
    }
  });
})();
