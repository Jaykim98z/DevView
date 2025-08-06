document.addEventListener("DOMContentLoaded", function () {
    const fileInput = document.getElementById("imageInput"); // ✔ HTML에 있는 ID로 수정
    const previewImage = document.getElementById("imagePreview"); // ✔ 실제 ID로 수정
    const deleteBtn = document.querySelector(".btn-delete"); // ✔ 클래스 기준 선택

    if (fileInput && previewImage) {
        fileInput.addEventListener("change", function () {
            const file = fileInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImage.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }

    if (deleteBtn) {
        deleteBtn.addEventListener("click", function (e) {
            e.preventDefault(); // 폼 제출 막기
            if (confirm("이미지를 삭제하시겠습니까?")) {
                fetch("/mypage/profile/image/delete", {
                    method: "POST"
                }).then(res => {
                    if (res.redirected) {
                        window.location.href = res.url;
                    } else {
                        alert("삭제 실패");
                    }
                });
            }
        });
    }
});
