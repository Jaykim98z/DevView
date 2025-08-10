document.addEventListener("DOMContentLoaded", function () {
    const fileInput = document.getElementById("imageInput");
    const previewImage = document.getElementById("imagePreview");
    const deleteBtn = document.querySelector(".btn-delete");

    // 🔹 이미지 미리보기
    if (fileInput && previewImage) {
        fileInput.addEventListener("change", function () {
            const file = fileInput.files[0];
            if (file) {
                // 🔸 이미지 크기 제한 (2MB)
                if (file.size > 2 * 1024 * 1024) {
                    alert("2MB 이하의 이미지만 업로드 가능합니다.");
                    fileInput.value = ""; // 파일 선택 취소
                    return;
                }

                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImage.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });

        // 드래그 앤 드롭 업로드 트리거
        const uploadTrigger = document.getElementById("uploadTrigger");
        if (uploadTrigger) {
            uploadTrigger.addEventListener("click", function () {
                fileInput.click();
            });
        }

        const dropArea = document.getElementById("dropArea");
        if (dropArea) {
            dropArea.addEventListener("dragover", function (e) {
                e.preventDefault();
                dropArea.classList.add("dragover");
            });

            dropArea.addEventListener("dragleave", function () {
                dropArea.classList.remove("dragover");
            });

            dropArea.addEventListener("drop", function (e) {
                e.preventDefault();
                dropArea.classList.remove("dragover");
                if (e.dataTransfer.files.length > 0) {
                    fileInput.files = e.dataTransfer.files;
                    const event = new Event("change");
                    fileInput.dispatchEvent(event);
                }
            });
        }
    }

    // 🔹 이미지 삭제 요청
    if (deleteBtn) {
        deleteBtn.addEventListener("click", function (e) {
            e.preventDefault();
            if (confirm("이미지를 삭제하시겠습니까?")) {
                fetch("/mypage/profile/image/delete", {
                    method: "POST",
                    headers: {
                        "X-CSRF-TOKEN": document.querySelector("input[name=_csrf]").value
                    }
                }).then(res => {
                    if (res.redirected) {
                        window.location.href = res.url;
                    } else {
                        alert("삭제에 실패했습니다.");
                    }
                }).catch(() => {
                    alert("삭제 요청 중 오류가 발생했습니다.");
                });
            }
        });
    }
});
