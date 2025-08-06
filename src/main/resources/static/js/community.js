document.addEventListener('click', (e) => {
  if (e.target.closest('.like-btn')) {
    const btn = e.target.closest('.like-btn');
    const icon = btn.querySelector('i');
    const count = btn.querySelector('span');
    const isLiked = btn.dataset.liked === 'true';
    const postId = btn.dataset.postId;
    const userId = btn.dataset.userId;

    if (isLiked) {
      fetch(`http://localhost:8080/api/community/posts/${postId}/likes/${userId}`, {
        method: 'DELETE',
      }).then(res => {
        if (res.ok) {
          btn.dataset.liked = 'false';
          icon.className = 'far fa-heart';
          count.textContent = parseInt(count.textContent) - 1;
          btn.classList.remove('active');
        } else {
          alert('좋아요 취소 실패');
        }
      }).catch(() => alert('서버 오류로 좋아요 취소 실패'));
    } else {
      fetch(`http://localhost:8080/api/community/posts/${postId}/likes/${userId}`, {
        method: 'POST',
      }).then(res => {
        if (res.ok) {
          btn.dataset.liked = 'true';
          icon.className = 'fas fa-heart';
          count.textContent = parseInt(count.textContent) + 1;
          btn.classList.add('active');
        } else {
          alert('좋아요 추가 실패');
        }
      }).catch(() => alert('서버 오류로 좋아요 추가 실패'));
    }
  }

  if (e.target.closest('.bookmark-btn')) {
    const btn = e.target.closest('.bookmark-btn');
    const icon = btn.querySelector('i');
    const count = btn.querySelector('span');
    const isBookmarked = btn.dataset.bookmarked === 'true';
    const postId = btn.dataset.postId;
    const userId = btn.dataset.userId;

    if (isBookmarked) {
      fetch(`http://localhost:8080/api/community/scraps/${btn.dataset.scrapId}`, {
        method: 'DELETE',
      }).then(res => {
        if (res.ok) {
          btn.dataset.bookmarked = 'false';
          icon.className = 'far fa-bookmark';
          count.textContent = parseInt(count.textContent) - 1;
          btn.classList.remove('active');
        } else {
          alert('스크랩 취소 실패');
        }
      }).catch(() => alert('서버 오류로 스크랩 취소 실패'));
    } else {
      fetch(`http://localhost:8080/api/community/posts/${postId}/scraps`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: userId }),
      }).then(res => {
        if (res.ok) {
          btn.dataset.bookmarked = 'true';
          icon.className = 'fas fa-bookmark';
          count.textContent = parseInt(count.textContent) + 1;
          btn.classList.add('active');
          res.json().then(data => {
            if (data.scrapId) btn.dataset.scrapId = data.scrapId;
          });
        } else {
          alert('스크랩 추가 실패');
        }
      }).catch(() => alert('서버 오류로 스크랩 추가 실패'));
    }
  }
});

document.addEventListener('click', (e) => {
  const card = e.target.closest('.post-card');
  if (card && !e.target.closest('.action-btn')) {
    const postId = card.dataset.id;
    console.log(`게시글 ${postId} 상세 페이지로 이동`);
  }
});

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

document.querySelectorAll('.filter-select').forEach(select => {
  select.addEventListener('change', (e) => {
    console.log(`필터 변경: ${e.target.id} = ${e.target.value}`);
  });
});
