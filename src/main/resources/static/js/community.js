'use strict';

/**
 * community.js
 * - list / detail / create 페이지 공용 스크립트
 * - 컨트롤러 매핑: /api/community/...
 */
document.addEventListener('DOMContentLoaded', function () {
  // ===== Page flags =====
  const PAGE = document.body.dataset.page || 'list';
  const IS_LIST   = PAGE === 'list';
  const IS_DETAIL = PAGE === 'detail';
  const IS_CREATE = PAGE === 'create';

  // ===== API prefix =====
  const API_PREFIX = '/api/community';
  const LIST_API   = `${API_PREFIX}/posts`;
  const CREATE_API = `${API_PREFIX}/posts`;
  const likeAddApiOf   = (id) => `${API_PREFIX}/posts/${id}/likes`;
  const likeDelApiOf   = (id) => `${API_PREFIX}/posts/${id}/likes`;
  const scrapAddApiOf  = (id) => `${API_PREFIX}/posts/${id}/scraps`;
  const scrapDelApiOf  = (id) => `${API_PREFIX}/posts/${id}/scraps`;
  const viewIncApiOf   = (id) => `${API_PREFIX}/posts/${id}/views`;
  const interviewLatestApi = `${API_PREFIX}/interview-results/latest`;

  // ===== 상태 =====
  let currentPage = 0;
  let pageSize = 12;
  let currentSort = 'createdAt,desc';
  let currentKeyword = '';
  let currentCategory = '';
  let currentLevel = '';
  const MAX_SIZE = 50;

  // ===== 엘리먼트 =====
  const cardListEl   = document.getElementById('cardList');
  const paginationEl = document.getElementById('pagination');
  const postCountEl  = document.getElementById('postCount') || document.querySelector('.post-count');

  const sortSelect   = document.getElementById('sortSelect');
  const sizeSelect   = document.getElementById('sizeSelect');
  const searchForm   = document.getElementById('searchForm');
  const keywordInput = document.getElementById('keywordInput');
  const categoryFilters = document.getElementById('categoryFilters');
  const levelFilters    = document.getElementById('levelFilters');

  // 글쓰기 폼
  const createForm = document.getElementById('community-post-form');

  // ===== CSRF =====
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  // ===== 페이지 분기 초기화 =====
  if (IS_LIST) {
    wireListEvents();
    loadPosts();
  } else if (IS_DETAIL) {
    wireDetailEvents();
    autoIncreaseView();
  } else if (IS_CREATE) {
    wireCreateEvents();
    // 필요 시 최신 인터뷰 결과 자동 채우기
    // prefillFromLatestInterview();
  }

  // =======================
  // List page
  // =======================
  function wireListEvents() {
    sortSelect?.addEventListener('change', () => {
      currentSort = sortSelect.value;
      currentPage = 0;
      loadPosts();
    });

    sizeSelect?.addEventListener('change', () => {
      pageSize = Math.min(parseInt(sizeSelect.value, 10) || 12, MAX_SIZE);
      currentPage = 0;
      loadPosts();
    });

    searchForm?.addEventListener('submit', (e) => {
      e.preventDefault();
      currentKeyword = keywordInput?.value?.trim() || '';
      currentPage = 0;
      loadPosts();
    });

    categoryFilters?.addEventListener('click', (e) => {
      if (e.target instanceof HTMLButtonElement) {
        currentCategory = e.target.dataset.category ?? '';
        currentPage = 0;
        loadPosts();
        [...categoryFilters.querySelectorAll('button')].forEach(b => b.classList.remove('active'));
        e.target.classList.add('active');
      }
    });

    levelFilters?.addEventListener('click', (e) => {
      if (e.target instanceof HTMLButtonElement) {
        currentLevel = e.target.dataset.level ?? '';
        currentPage = 0;
        loadPosts();
        [...levelFilters.querySelectorAll('button')].forEach(b => b.classList.remove('active'));
        e.target.classList.add('active');
      }
    });
  }

  async function loadPosts() {
    if (!cardListEl) return;

    const params = new URLSearchParams();
    params.set('page', String(currentPage));
    params.set('size', String(pageSize));
    if (currentSort)     params.append('sort', currentSort);
    // (서버 미지원일 수 있음) 백엔드 확장 전까지는 무해
    if (currentKeyword)  params.set('query', currentKeyword);
    if (currentCategory) params.set('category', currentCategory);
    if (currentLevel)    params.set('level', currentLevel);

    const url = `${LIST_API}?${params.toString()}`;

    try {
      const page = await fetchJson(url);
      renderPosts(page.content || []);
      renderPagination(page);
      updateCount(page.totalElements || 0);
    } catch (err) {
      console.error(err);
      cardListEl.innerHTML = `<p style="padding:16px;">목록을 불러오지 못했습니다.</p>`;
      if (paginationEl) paginationEl.innerHTML = '';
      updateCount(0);
    }
  }

  function renderPosts(items) {
    if (!items.length) {
      cardListEl.innerHTML = `<p style="padding:16px;">게시글이 없습니다.</p>`;
      return;
    }

    cardListEl.innerHTML = items.map(p => {
      const postId     = p.postId ?? p.id;
      const writerName = p.writerName ?? '익명';
      const levelTag   = p.level ?? '';
      const techTag    = p.techTag ?? '';
      const createdAt  = formatDate(p.createdAt);

      const likeStat = `
        <span class="icon-stat is-readonly" aria-label="좋아요 수">
          <i class="fa fa-heart"></i> <span class="count" data-like-count="${postId}">${p.likeCount ?? 0}</span>
        </span>`;
      const scrapStat = `
        <span class="icon-stat is-readonly" aria-label="스크랩 수">
          <i class="fa fa-bookmark"></i> <span class="count" data-scrap-count="${postId}">${p.scrapCount ?? 0}</span>
        </span>`;

      return `
        <div class="post-card" data-post-id="${postId}">
          <div class="user-info">
            <img src="/img/profile-default.svg" alt="프로필" class="profile-img" />
            <div>
              <strong>${escapeHtml(writerName)}</strong>
              <span class="user-meta">${escapeHtml(techTag)} · ${escapeHtml(levelTag)}</span>
            </div>
          </div>

          <h3 class="post-title">${escapeHtml(p.title ?? '')}</h3>
          <p class="post-summary">${escapeHtml(p.summary ?? '')}</p>

          <div class="post-score">${p.score ?? 0}점 · ${escapeHtml(p.grade ?? '')}</div>

          <div class="post-meta">
            ${likeStat}
            ${scrapStat}
            <span class="icon-stat" aria-label="조회수">
              <i class="fa fa-eye"></i> <span class="count">${p.viewCount ?? 0}</span>
            </span>
            <span class="created-at">${createdAt}</span>
          </div>

          <a class="detail-link" href="/community/posts/${postId}/detail">자세히 보기</a>
        </div>
      `;
    }).join('');
  }

  function renderPagination(page) {
    if (!paginationEl) return;

    const totalPages = page.totalPages ?? 0;
    const number     = page.number ?? 0;
    const first      = page.first ?? true;
    const last       = page.last ?? true;

    if (totalPages <= 1) {
      paginationEl.innerHTML = '';
      return;
    }

    const buttons = [];
    buttons.push(`<button class="page-btn" data-page="prev" ${first ? 'disabled' : ''}>이전</button>`);

    const start = Math.max(0, number - 2);
    const end   = Math.min(totalPages - 1, number + 2);
    for (let i = start; i <= end; i++) {
      buttons.push(`<button class="page-btn ${i === number ? 'active' : ''}" data-page="${i}">${i + 1}</button>`);
    }

    buttons.push(`<button class="page-btn" data-page="next" ${last ? 'disabled' : ''}>다음</button>`);
    paginationEl.innerHTML = buttons.join('');

    paginationEl.querySelectorAll('.page-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const val = btn.getAttribute('data-page');
        if (val === 'prev' && !first) currentPage = Math.max(0, number - 1);
        else if (val === 'next' && !last) currentPage = Math.min(totalPages - 1, number + 1);
        else if (!isNaN(Number(val))) currentPage = Number(val);
        loadPosts();
      });
    });
  }

  function updateCount(total) {
    if (postCountEl) postCountEl.textContent = `총 ${total}개의 면접 결과`;
  }

  // =======================
  // Detail page
  // =======================
  function wireDetailEvents() {
    document.body.addEventListener('click', async (e) => {
      const likeBtn  = e.target.closest('[data-like-btn], .btn-like, .like-btn');
      const scrapBtn = e.target.closest('[data-scrap-btn], .btn-scrap, .bookmark-btn');
      if (!likeBtn && !scrapBtn) return;

      if (likeBtn)  await toggleLike(likeBtn);
      if (scrapBtn) await toggleScrap(scrapBtn);
    });
  }

  async function toggleLike(btn) {
    const postId = btn.dataset.postId || document.querySelector('[data-post-id]')?.dataset.postId;
    if (!postId) return alert('잘못된 요청입니다.');

    const willActivate = !btn.classList.contains('active');
    try {
      const data = await fetchJson(
        willActivate ? likeAddApiOf(postId) : likeDelApiOf(postId),
        { method: willActivate ? 'POST' : 'DELETE' }
      );

      btn.classList.toggle('active', data?.active ?? willActivate);
      btn.setAttribute('aria-pressed', String(btn.classList.contains('active')));

      const countEl =
        document.querySelector(`[data-like-count="${postId}"]`) ||
        btn.querySelector('.count') || btn.querySelector('span');
      if (countEl && typeof data?.count === 'number') countEl.textContent = String(data.count);
    } catch (err) {
      if (err.message === 'UNAUTHORIZED') alert('로그인이 필요합니다.');
      else alert('좋아요 처리 중 오류가 발생했습니다.');
    }
  }

  async function toggleScrap(btn) {
    const postId = btn.dataset.postId || document.querySelector('[data-post-id]')?.dataset.postId;
    if (!postId) return alert('잘못된 요청입니다.');

    const willActivate = !btn.classList.contains('active');
    try {
      const data = await fetchJson(
        willActivate ? scrapAddApiOf(postId) : scrapDelApiOf(postId),
        { method: willActivate ? 'POST' : 'DELETE' }
      );

      btn.classList.toggle('active', data?.active ?? willActivate);
      btn.setAttribute('aria-pressed', String(btn.classList.contains('active')));

      const countEl =
        document.querySelector(`[data-scrap-count="${postId}"]`) ||
        btn.querySelector('.count') || btn.querySelector('span');
      if (countEl && typeof data?.count === 'number') countEl.textContent = String(data.count);
    } catch (err) {
      if (err.message === 'UNAUTHORIZED') alert('로그인이 필요합니다.');
      else alert('스크랩 처리 중 오류가 발생했습니다.');
    }
  }

  async function autoIncreaseView() {
    const postId = document.querySelector('[data-post-id]')?.dataset.postId;
    if (!postId) return;
    try { await fetchJson(viewIncApiOf(postId), { method: 'POST' }); } catch {}
  }

  // =======================
  // Create page (글 등록)
  // =======================
  function wireCreateEvents() {
    if (!createForm) {
      console.warn('[community] create form not found');
      return;
    }
    createForm.addEventListener('submit', onCreateSubmit);
  }

  async function onCreateSubmit(e) {
    e.preventDefault();

    const title   = createForm.querySelector('[name="title"]')?.value?.trim();
    const content = createForm.querySelector('[name="content"]')?.value?.trim();
    const summary = createForm.querySelector('[name="summary"]')?.value?.trim() || null;
    const interviewType = createForm.querySelector('[name="interviewType"]')?.value || null;

    if (!title || !content) {
      alert('제목과 내용을 입력해주세요.');
      return;
    }

    try {
      const payload = { title, content, summary, interviewType };
      const res = await fetch(CREATE_API, {
        method: 'POST',
        headers: buildJsonHeaders(),
        credentials: 'same-origin',
        body: JSON.stringify(payload),
      });

      if (res.status === 401) {
        alert('로그인이 필요합니다.');
        return;
      }
      if (res.status === 403) {
        alert('보안 토큰(CSRF) 오류입니다. 새로고침 후 다시 시도해주세요.');
        return;
      }
      if (!res.ok) {
        const t = await res.text().catch(() => '');
        console.error('등록 실패', res.status, t);
        alert(`등록 실패 (${res.status})`);
        return;
      }

      const saved = await res.json().catch(() => ({}));
      const postId = saved?.postId ?? saved?.id ?? null;
      window.location.href = postId ? `/community/posts/${postId}/detail` : '/community';
    } catch (err) {
      console.error(err);
      alert('네트워크 오류로 등록에 실패했습니다.');
    }
  }

  // (선택) 최신 인터뷰 결과로 제목/요약 자동 채우기
  async function prefillFromLatestInterview() {
    try {
      const dto = await fetchJson(interviewLatestApi);
      if (!dto) return;
      const $ = (s) => createForm?.querySelector(s);
      if ($ && $('[name="title"]') && dto.title)   $('[name="title"]').value = dto.title;
      if ($ && $('[name="summary"]') && dto.summary) $('[name="summary"]').value = dto.summary;
      if ($ && $("[name='interviewType']") && dto.interviewType) $("[name='interviewType']").value = dto.interviewType;
    } catch {}
  }

  // =======================
  // Fetch helpers / Utils
  // =======================
  function buildJsonHeaders() {
    const h = { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfToken && csrfHeader) h[csrfHeader] = csrfToken;
    return h;
  }

  async function fetchJson(url, { method = 'GET', bodyObj } = {}) {
    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    if (bodyObj) headers['Content-Type'] = 'application/json';

    const res = await fetch(url, {
      method,
      headers,
      credentials: 'same-origin',
      body: bodyObj ? JSON.stringify(bodyObj) : undefined
    });

    if (res.status === 401) throw new Error('UNAUTHORIZED');
    if (!res.ok) throw new Error('REQUEST_FAILED');

    const text = await res.text();
    return text ? JSON.parse(text) : {};
  }

  function formatDate(dt) {
    if (!dt) return '';
    try {
      const d = new Date(dt);
      if (isNaN(d.getTime())) return String(dt);
      return d.toLocaleString();
    } catch {
      return String(dt);
    }
  }

  function escapeHtml(str) {
    return String(str)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }
});
