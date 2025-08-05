// main.js
document.addEventListener("DOMContentLoaded", () => {

    // 부드러운 스크롤
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener("click", function (e) {
            e.preventDefault();
            const targetId = this.getAttribute("href").substring(1);
            const target = document.getElementById(targetId);
            if (target) {
                target.scrollIntoView({ behavior: "smooth" });
            }
        });
    });

    // 면접 시작 버튼 로딩 애니메이션
    const startBtn = document.querySelector('.btn-primary');
    if (startBtn) {
        startBtn.addEventListener("click", (e) => {
            e.preventDefault();

            // 로딩 오버레이 생성
            const overlay = document.createElement("div");
            overlay.style.position = "fixed";
            overlay.style.top = 0;
            overlay.style.left = 0;
            overlay.style.width = "100%";
            overlay.style.height = "100%";
            overlay.style.backgroundColor = "rgba(255,255,255,0.9)";
            overlay.style.display = "flex";
            overlay.style.justifyContent = "center";
            overlay.style.alignItems = "center";
            overlay.style.zIndex = 9999;

            const spinner = document.createElement("div");
            spinner.style.border = "6px solid #f3f3f3";
            spinner.style.borderTop = "6px solid #3498DB";
            spinner.style.borderRadius = "50%";
            spinner.style.width = "50px";
            spinner.style.height = "50px";
            spinner.style.animation = "spin 1s linear infinite";

            overlay.appendChild(spinner);
            document.body.appendChild(overlay);

            // 애니메이션 스타일 추가
            const style = document.createElement("style");
            style.innerHTML = `
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `;
            document.head.appendChild(style);

            // 1.5초 후 면접 페이지 이동
            setTimeout(() => {
                window.location.href = startBtn.getAttribute("href");
            }, 1500);
        });
    }
});
