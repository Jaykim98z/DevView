document.addEventListener("DOMContentLoaded", function () {
  const IS_DETAIL = document.body.dataset.page === "detail";

  let currentPage = 0;
  let pageSize = 12;
  let currentSort = "createdAt,desc";
  let currentKeyword = ""; // 서버가 아직 검색 파라미터를 받지 않으면 이 값은 미사용
  let currentCategory = "";
  let currentLevel = "";

  const MAX_SIZE = 50;
  const cardListEl   = document.getElementById("cardList");
  const paginationEl = document.getElementById("pagination");
  const postCountEl  = document.getElementById("postCount") || document.querySelector(".post-count");

  const sortSelect   = document.getElementById("sortSelect");
  const sizeSelect   = document.getElementById("sizeSelect");
  const searchForm   = document.getElementById("searchForm");
  const keywordInput = document.getElementById("keywordInput");
  const categoryFilters = document.getElementById("categoryFilters");
  const levelFilters    = document.getElementById("levelFilters");

  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

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
    currentKeyword = keywordInput?.value?.trim() || "";
    currentPage = 0;
    // 서버에서 검색 미지원이면 주석 처리하거나 서버 구현 후 사용
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
      const likeBtn  = e.target.closest("[data-like-btn], .btn-like, .like-btn");
      const scrapBtn = e.target.closest("[data-scrap-btn], .btn-scrap, .bookmark-btn");
      if (!likeBtn && !scrapBtn) return;

      if (likeBtn)  await handleLike(likeBtn);
      if (scrapBtn) await handleScrap(scrapBtn);
    });
  }

  async function loadPosts() {
    if (!cardListEl) return;

    const params = new URLSearchParams();
    params.set("page", String(currentPage));
    params.set("size", String(pageSize));
    if (currentSort) params.set("sort", currentSort);

    // 검색 파라미터는 서버에서 지원할 때만 아래 라인 활성화
    // if (currentKeyword) params.set("keyword", currentKeyword);

    if (currentCategory) params.set("category", currentCategory);
    if (currentLevel) params.set("level", currentLevel);

    const url = `/api/community/posts?${params.toString()}`;

    try {
      const res = await fetch(url, { method: "GET", credentials: "same-origin" });
      if (!res.ok) throw new Error(`목록 조회 실패: ${res.status}`);
      const page = await res.json();

      renderPosts(page.content || []);
      renderPagination(page);
      updateCount(page.totalElements ?? 0);
    } catch (err) {
      console.error(err);
      cardListEl.innerHTML = `<p style="padding:16px;">목록을 불러오지 못했습니다.</p>`;
      if (paginationEl) paginationEl.innerHTML = "";
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
      const writerName = p.writerName ?? p.username ?? "익명";
      const levelVal   = p.level ?? "";
      const techTag    = p.techTag ?? "";
      const createdAt  = formatDate(p.createdAt);

      const likeCount  = p.likeCount ?? 0;
      const scrapCount = p.scrapCount ?? 0;
      const viewCount  = p.viewCount ?? 0;

      const scoreRaw = (p.score ?? p.totalScore ?? p.finalScore);
      const hasScore = !(scoreRaw === null || scoreRaw === undefined);
      const scoreText = hasScore ? `${scoreRaw}점` : "-";

      const gradeRaw = (p.grade ?? p.gradeLabel ?? p.gradeName);
      const gradeText = gradeRaw ? escapeHtml(String(gradeRaw)) : "-";

      const likeStat = `
        <span class="icon-stat is-readonly" aria-label="좋아요 수">
          <i class="fa fa-heart"></i> <span class="count" data-like-count="${postId}">${likeCount}</span>
        </span>`;
      const scrapStat = `
        <span class="icon-stat is-readonly" aria-label="스크랩 수">
          <i class="fa fa-bookmark"></i> <span class="count" data-scrap-count="${postId}">${scrapCount}</span>
        </span>`;

      return `
        <div class="post-card" data-post-id="${postId}">
          <div class="user-info">
            <img src="/img/profile-default.svg" alt="프로필" class="profile-img" />
            <div>
              <strong>${escapeHtml(writerName)}</strong>
              <span class="user-meta">${escapeHtml(techTag)} · ${escapeHtml(levelVal)}</span>
            </div>
          </div>

          <h3 class="post-title">${escapeHtml(p.title ?? "")}</h3>
          <p class="post-summary">${escapeHtml(p.summary ?? "")}</p>

          <div class="post-score">${scoreText} · ${gradeText}</div>

          <div class="post-meta">
            ${likeStat}
            ${scrapStat}
            <span class="icon-stat" aria-label="조회수">
              <i class="fa fa-eye"></i> <span class="count">${viewCount}</span>
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
    const number     = page.number ?? 0;
    const first      = page.first ?? true;
    const last       = page.last ?? true;

    if (totalPages <= 1) {
      paginationEl.innerHTML = "";
      return;
    }

    const buttons = [];
    buttons.push(`<button class="page-btn" data-page="prev" ${first ? "disabled" : ""}>이전</button>`);

    const start = Math.max(0, number - 2);
    const end   = Math.min(totalPages - 1, number + 2);
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
    const postId = btn.dataset.postId || document.querySelector("[data-post-id]")?.dataset.postId;
    if (!postId) return alert("잘못된 요청입니다.");

    // 상세 페이지에서만 사용. 컨트롤러 스펙에 맞춰 userId 필요
    const userId = document.body.dataset.userId;
    if (!userId) return alert("로그인이 필요합니다.");

    try {
      const res = await fetch(`/api/community/posts/${postId}/likes/${userId}`, {
        method: btn.classList.contains("active") ? "DELETE" : "POST",
        headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
        credentials: "same-origin"
      });
      if (res.status === 401) throw new Error("UNAUTHORIZED");
      if (!res.ok) throw new Error("REQUEST_FAILED");

      btn.classList.toggle("active");
      btn.setAttribute("aria-pressed", String(btn.classList.contains("active")));

      const countEl =
        document.querySelector(`[data-like-count="${postId}"]`) ||
        btn.querySelector(".count") || btn.querySelector("span");
      if (countEl) {
        // 서버가 새 count를 안 보내므로 프론트에서 증감 처리
        const cur = parseInt(countEl.textContent || "0", 10);
        countEl.textContent = String(btn.classList.contains("active") ? cur + 1 : Math.max(0, cur - 1));
      }
    } catch (err) {
      if (err.message === "UNAUTHORIZED") alert("로그인이 필요합니다.");
      else alert("좋아요 처리 중 오류가 발생했습니다.");
    }
  }

  async function handleScrap(btn) {
    const postId = btn.dataset.postId || document.querySelector("[data-post-id]")?.dataset.postId;
    if (!postId) return alert("잘못된 요청입니다.");

    try {
      const res = await fetch(`/api/community/posts/${postId}/scraps`, {
        method: "POST",
        headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
        credentials: "same-origin",
        body: null
      });
      if (res.status === 401) throw new Error("UNAUTHORIZED");
      if (!res.ok) throw new Error("REQUEST_FAILED");

      btn.classList.toggle("active");
      btn.setAttribute("aria-pressed", String(btn.classList.contains("active")));

      const countEl =
        document.querySelector(`[data-scrap-count="${postId}"]`) ||
        btn.querySelector(".count") || btn.querySelector("span");
      if (countEl) {
        const cur = parseInt(countEl.textContent || "0", 10);
        countEl.textContent = String(btn.classList.contains("active") ? cur + 1 : Math.max(0, cur - 1));
      }
    } catch (err) {
      if (err.message === "UNAUTHORIZED") alert("로그인이 필요합니다.");
      else alert("스크랩 처리 중 오류가 발생했습니다.");
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
