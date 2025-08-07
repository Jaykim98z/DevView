document.addEventListener("DOMContentLoaded", function () {
  // 좋아요 버튼 클릭
  document.querySelectorAll(".like-btn").forEach(button => {
    button.addEventListener("click", () => {
      const postId = button.dataset.postId;
      const userId = button.dataset.userId;

      // API 호출 예시
      fetch(`/api/community/posts/${postId}/likes/${userId}`, {
        method: "POST"
      })
        .then(res => {
          if (!res.ok) throw new Error("좋아요 실패");
          return res.json();
        })
        .then(() => {
          const countSpan = button.querySelector("span");
          let count = parseInt(countSpan.textContent);
          countSpan.textContent = count + 1;
          button.classList.add("active");
        })
        .catch(err => console.error("좋아요 실패:", err));
    });
  });

  // 스크랩 버튼 클릭
  document.querySelectorAll(".bookmark-btn").forEach(button => {
    button.addEventListener("click", () => {
      const postId = button.dataset.postId;
      const userId = button.dataset.userId;

      fetch(`/api/community/posts/${postId}/scraps`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: userId,
          postId: postId
        })
      })
        .then(res => {
          if (!res.ok) throw new Error("스크랩 실패");
          return res.json();
        })
        .then(() => {
          const countSpan = button.querySelector("span");
          let count = parseInt(countSpan.textContent);
          countSpan.textContent = count + 1;
          button.classList.add("active");
        })
        .catch(err => console.error("스크랩 실패:", err));
    });
  });

  // 필터, 검색 기능은 나중에 구현
});
