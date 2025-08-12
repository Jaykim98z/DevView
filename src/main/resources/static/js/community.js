document.addEventListener("DOMContentLoaded", function () {
  const IS_DETAIL = document.body.dataset.page === "detail";

  let currentPage = 0;
  let pageSize = 12;
  let currentSort = "createdAt,desc";
  let currentKeyword = "";
  let currentCategory = "";
  let currentLevel = "";

  const MAX_SIZE = 50;
  const cardListEl = document.getElementById("cardList");
  const paginationEl = document.getElementById("pagination");
  const postCountEl = document.getElementById("postCount") || document.querySelector(".post-count");

  const sortSelect = document.getElementById("sortSelect");
  const sizeSelect = document.getElementById("sizeSelect");
  const searchForm = document.getElementById("searchForm");
  const keywordInput = document.getElementById("keywordInput");
  const categoryFilters = document.getElementById("categoryFilters");
  const levelFilters = document.getElementById("levelFilters");

  if (!IS_DETAIL) loadPosts();

  sortSelect?.addEventListener("change", () => {
    currentSort = sortSelect.value;
    currentPage = 0;
    loadPosts();
  });

  sizeSelect?.addEventListener("change", () => {
    pageSize = Math.min(parseInt(sizeSelect.value, 10) || 12, MAX_SIZE);
    currentPage = 0;
    loadPosts();
  });

  searchForm?.addEventListener("submit", (e) => {
    e.preventDefault();
    currentKeyword = keywordInput.value.trim();
    currentPage = 0;
    loadPosts();
  });

  categoryFilters?.addEventListener("click", (e) => {
    if (e.target instanceof HTMLButtonElement) {
      currentCategory = e.target.dataset.category ?? "";
      currentPage = 0;
      loadPosts();
      [...categoryFilters.querySelectorAll("button")].forEach(b => b.classList.remove("active"));
      e.target.classList.add("active");
    }
  });

  levelFilters?.addEventListener("click", (e) => {
    if (e.target instanceof HTMLButtonElement) {
      currentLevel = e.target.dataset.level ?? "";
      currentPage = 0;
      loadPosts();
      [...levelFilters.querySelectorAll("button")].forEach(b => b.classList.remove("active"));
      e.target.classList.add("active");
    }
  });

  if (IS_DETAIL) {
    document.body.addEventListener("click", async (e) => {
      const likeBtn = e.target.closest(".btn-like, .like-btn");
      const scrapBtn = e.target.closest(".btn-scrap, .bookmark-btn");
      if (!likeBtn && !scrapBtn) return;

      if (likeBtn) await handleLike(likeBtn);
      else if (scrapBtn) await handleScrap(scrapBtn);
    });
  }

  async function loadPosts() {
    if (!cardListEl) return;
    const params = new URLSearchParams();
    params.set("page", String(currentPage));
    params.set("size", String(pageSize));
    if (currentSort) params.append("sort", currentSort);

    // if (currentCategory) params.set("category", currentCategory);
    // if (currentLevel) params.set("level", currentLevel);
    // if (currentKeyword) params.set("query", currentKeyword);

    const url = `/api/community/posts?${params.toString()}`;
    try {
      const res = await fetch(url, { method: "GET" });
      if (!res.ok) throw new Error(`목록 조회 실패: ${res.status}`);
      const page = await res.json(); // Spring Page

      renderPosts(page.content || []);
      renderPagination(page);
      updateCount(page.totalElements || 0);
    } catch (err) {
      console.error(err);
      cardListEl.innerHTML = `<p style="padding:16px;">목록을 불러오지 못했습니다.</p>`;
      paginationEl && (paginationEl.innerHTML = "");
      updateCount(0);
    }
  }

  function renderPosts(items) {
    if (!items.length) {
      cardListEl.innerHTML = `<p style="padding:16px;">게시글이 없습니다.</p>`;
      return;
    }

    cardListEl.innerHTML = items.map(p => {
      const postId = p.postId ?? p.id;
      const writerName = p.writerName ?? "익명";
      const levelTag = p.level ?? "";
      const techTag = p.techTag ?? "";
      const createdAt = formatDate(p.createdAt);

      const likeStat = `
        <span class="icon-stat is-readonly" aria-label="좋아요 수">
          <i class="fa fa-heart"></i> <span class="count">${p.likeCount ?? 0}</span>
        </span>`;
      const scrapStat = `
        <span class="icon-stat is-readonly" aria-label="스크랩 수">
          <i class="fa fa-bookmark"></i> <span class="count">${p.scrapCount ?? 0}</span>
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

          <h3 class="post-title">${escapeHtml(p.title ?? "")}</h3>
          <p class="post-summary">${escapeHtml(p.summary ?? "")}</p>

          <div class="post-score">${p.score ?? 0}점 · ${escapeHtml(p.grade ?? "")}</div>

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
    }).join("");
  }

  function renderPagination(page) {
    if (!paginationEl) return;
    const totalPages = page.totalPages ?? 0;
    const number = page.number ?? 0;
    const first = page.first ?? true;
    const last = page.last ?? true;

    if (totalPages <= 1) {
      paginationEl.innerHTML = "";
      return;
    }

    const buttons = [];
    buttons.push(`<button class="page-btn" data-page="prev" ${first ? "disabled" : ""}>이전</button>`);

    const start = Math.max(0, number - 2);
    const end = Math.min(totalPages - 1, number + 2);
    for (let i = start; i <= end; i++) {
      buttons.push(`<button class="page-btn ${i === number ? "active" : ""}" data-page="${i}">${i + 1}</button>`);
    }

    buttons.push(`<button class="page-btn" data-page="next" ${last ? "disabled" : ""}>다음</button>`);
    paginationEl.innerHTML = buttons.join("");

    paginationEl.querySelectorAll(".page-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const val = btn.getAttribute("data-page");
        if (val === "prev" && !first) currentPage = Math.max(0, number - 1);
        else if (val === "next" && !last) currentPage = Math.min(totalPages - 1, number + 1);
        else if (!isNaN(Number(val))) currentPage = Number(val);
        loadPosts();
      });
    });
  }

  function updateCount(total) {
    if (postCountEl) postCountEl.textContent = `총 ${total}개의 면접 결과`;
  }

  async function handleLike(btn) {
    const postId = btn.dataset.postId;
    const userId = btn.dataset.userId || window.CURRENT_USER_ID;
    if (!postId || !userId) {
      alert("로그인이 필요합니다.");
      return;
    }
    try {
      const res = await fetch(`/api/community/posts/${postId}/likes/${userId}`, { method: "POST" });
      if (!res.ok) throw new Error("좋아요 실패");

      const countEl = btn.querySelector(".count") || btn.querySelector("span");
      const next = (parseInt(countEl?.textContent || "0", 10) || 0) + 1;
      if (countEl) countEl.textContent = String(next);

      btn.classList.add("active");
      btn.setAttribute("aria-pressed", "true");
    } catch (e) {
      console.error(e);
      alert("좋아요에 실패했습니다.");
    }
  }

  async function handleScrap(btn) {
    const postId = btn.dataset.postId;
    const userId = btn.dataset.userId || window.CURRENT_USER_ID;
    if (!postId || !userId) {
      alert("로그인이 필요합니다.");
      return;
    }
    try {
      const res = await fetch(`/api/community/posts/${postId}/scraps`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId, postId })
      });
      if (!res.ok) throw new Error("스크랩 실패");

      const countEl = btn.querySelector(".count") || btn.querySelector("span");
      const next = (parseInt(countEl?.textContent || "0", 10) || 0) + 1;
      if (countEl) countEl.textContent = String(next);

      btn.classList.add("active");
      btn.setAttribute("aria-pressed", "true");
    } catch (e) {
      console.error(e);
      alert("스크랩에 실패했습니다.");
    }
  }

  function formatDate(dt) {
    if (!dt) return "";
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
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }
});
