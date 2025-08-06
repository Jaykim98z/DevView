document.addEventListener('click', (e) => {
  if (e.target.closest('.like-btn')) {
    const btn = e.target.closest('.like-btn');
    const icon = btn.querySelector('i');
    const count = btn.querySelector('span');
    const isLiked = btn.dataset.liked === 'true';

    if (isLiked) {
      btn.dataset.liked = 'false';
      icon.className = 'far fa-heart';
      count.textContent = parseInt(count.textContent) - 1;
      btn.classList.remove('active');
    } else {
      btn.dataset.liked = 'true';
      icon.className = 'fas fa-heart';
      count.textContent = parseInt(count.textContent) + 1;
      btn.classList.add('active');
    }
  }

  if (e.target.closest('.bookmark-btn')) {
    const btn = e.target.closest('.bookmark-btn');
    const icon = btn.querySelector('i');
    const count = btn.querySelector('span');
    const isBookmarked = btn.dataset.bookmarked === 'true';

    if (isBookmarked) {
      btn.dataset.bookmarked = 'false';
      icon.className = 'far fa-bookmark';
      count.textContent = parseInt(count.textContent) - 1;
      btn.classList.remove('active');
    } else {
      btn.dataset.bookmarked = 'true';
      icon.className = 'fas fa-bookmark';
      count.textContent = parseInt(count.textContent) + 1;
      btn.classList.add('active');
    }
  }
});

// 카드 클릭 시 상세 페이지 이동
document.addEventListener('click', (e) => {
  const card = e.target.closest('.post-card');
  if (card && !e.target.closest('.action-btn')) {
    const postId = card.dataset.id;
    console.log(`게시글 ${postId} 상세 페이지로 이동`);
    // location.href = `/community/${postId}`; // 실제 경로 연결 시
  }
});

// 검색 기능
document.querySelectorAll('.search-btn').forEach(btn => {
  btn.addEventListener('click', (e) => {
    e.preventDefault();
    const searchInput = btn.previousElementSibling || document.querySelector('.search-input');
    const query = searchInput.value.trim();
    if (query) {
      console.log(`검색: ${query}`);
    }
  });
});

// 필터 변경
document.querySelectorAll('.filter-select').forEach(select => {
  select.addEventListener('change', (e) => {
    console.log(`필터 변경: ${e.target.id} = ${e.target.value}`);
  });
});