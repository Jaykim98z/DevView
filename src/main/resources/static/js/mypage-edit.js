document.addEventListener("DOMContentLoaded", function () {
    const fileInput     = document.getElementById("imageInput");
    const previewImage  = document.getElementById("imagePreview");
    const deleteBtn     = document.querySelector(".btn-delete");
    const uploadTrigger = document.getElementById("uploadTrigger");
    const dropArea      = document.getElementById("dropArea");
    const submitBtn     = document.getElementById("btnSubmitImage");
    const uploadForm    = document.getElementById("imageUploadForm");

    // ✅ HTML 안내 기준: 최대 5MB / JPG·JPEG·PNG
    const MAX = 5 * 1024 * 1024;
    const okTypes = ["image/jpeg", "image/jpg", "image/png"];

    // 윈도우 전역: 브라우저가 파일을 바로 여는 기본 동작 방지
    ["dragover", "drop"].forEach((ev) => {
        window.addEventListener(ev, (e) => e.preventDefault());
    });

    function setSubmitEnabled(enabled) {
        if (!submitBtn) return;
        submitBtn.disabled = !enabled;
    }

    function validate(file) {
        if (!file) return false;
        if (!okTypes.includes(file.type)) {
            alert("JPG/JPEG/PNG 형식만 업로드할 수 있습니다.");
            return false;
        }
        if (file.size > MAX) {
            alert("5MB 이하의 이미지만 업로드 가능합니다.");
            return false;
        }
        return true;
    }

    function preview(file) {
        const reader = new FileReader();
        reader.onload = (e) => { if (previewImage) previewImage.src = e.target.result; };
        reader.readAsDataURL(file);
    }

    function assignFileToInput(file) {
        try {
            const dt = new DataTransfer();
            dt.items.add(file);
            fileInput.files = dt.files;
        } catch (e) {
            try { fileInput.files = [file]; } catch (_) { /* noop */ }
        }
    }

    function onPick(file) {
        if (!validate(file)) {
            if (uploadForm) uploadForm.reset();
            setSubmitEnabled(false);
            return;
        }
        preview(file);
        setSubmitEnabled(true);
    }

    // ====== CSRF/JWT 헤더 빌더 ======
    function buildHeaders() {
        const headers = new Headers();

        // JWT (있다면)
        const maybeGetAccessToken = (typeof window !== "undefined")
            ? window["getAccessToken"]
            : undefined;
        if (typeof maybeGetAccessToken === "function") {
            const token = maybeGetAccessToken();
            if (token) headers.set("Authorization", "Bearer " + token);
        }

        // CSRF (meta 우선)
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        const csrfTokenMeta  = document.querySelector('meta[name="_csrf"]');
        if (csrfHeaderMeta && csrfTokenMeta) {
            headers.set(csrfHeaderMeta.content, csrfTokenMeta.content);
        } else {
            const csrfInput = document.querySelector('input[name="_csrf"]');
            if (csrfInput) headers.set("X-CSRF-TOKEN", csrfInput.value);
        }
        return headers;
    }

    // ====== 파일 선택 ======
    if (fileInput) {
        fileInput.addEventListener("change", function () {
            const file = fileInput.files && fileInput.files[0];
            if (file) onPick(file);
            else setSubmitEnabled(false);
        });
    }

    // 클릭 트리거(버튼)
    if (uploadTrigger && fileInput) {
        uploadTrigger.addEventListener("click", () => fileInput.click());
    }

    // 업로드 박스 클릭/키보드 접근
    if (dropArea && fileInput) {
        dropArea.addEventListener("click", (e) => {
            if (e.target === dropArea) fileInput.click();
        });
        dropArea.addEventListener("keydown", (e) => {
            if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                fileInput.click();
            }
        });
    }

    // 드래그&드랍
    if (dropArea && fileInput) {
        ["dragenter", "dragover"].forEach((ev) =>
            dropArea.addEventListener(ev, (e) => {
                e.preventDefault();
                dropArea.classList.add("dragover");
            })
        );
        ["dragleave", "drop"].forEach((ev) =>
            dropArea.addEventListener(ev, (e) => {
                e.preventDefault();
                dropArea.classList.remove("dragover");
            })
        );
        dropArea.addEventListener("drop", (e) => {
            const files = e.dataTransfer && e.dataTransfer.files;
            if (files && files.length > 0) {
                const file = files[0];
                assignFileToInput(file);
                onPick(file);
            }
        });
    }

    // ====== 업로드(저장): PUT /api/mypage/profile ======
    if (uploadForm && fileInput) {
        uploadForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            if (!fileInput.files || fileInput.files.length === 0) {
                alert("업로드할 이미지를 선택해주세요.");
                return;
            }

            // 중복 제출 방지
            if (submitBtn && !submitBtn.disabled) {
                submitBtn.disabled = true;
                submitBtn.dataset.originalText = submitBtn.textContent;
                submitBtn.textContent = "업로드 중...";
            }

            // profile JSON (존재하는 입력만 포함)
            const profile = {
                name: document.getElementById("name")?.value ?? undefined,
                job: document.getElementById("job")?.value ?? undefined,
                careerLevel: document.getElementById("careerLevel")?.value ?? undefined
            };
            Object.keys(profile).forEach(k => profile[k] === undefined && delete profile[k]);

            const formData = new FormData();
            formData.append("profile", new Blob([JSON.stringify(profile)], { type: "application/json" }));
            formData.append("profileImage", fileInput.files[0]);

            try {
                const res = await fetch("/api/mypage/profile", {
                    method: "PUT",
                    headers: buildHeaders(),
                    body: formData,
                    credentials: "same-origin"
                });
                if (!res.ok) throw new Error("업로드 실패 " + res.status);

                const data = await res.json().catch(() => ({}));
                const newUrl = data && typeof data === "object" ? data["profileImageUrl"] : null;
                if (newUrl && previewImage) {
                    previewImage.src = newUrl;
                }
                alert("프로필 이미지가 저장되었습니다.");
            } catch (err) {
                console.error(err);
                alert("저장 중 오류가 발생했습니다.");
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = submitBtn.dataset.originalText || "업로드";
                }
            }
        });
    }

    // ====== 삭제: DELETE /api/mypage/profile/image ======
    if (deleteBtn) {
        deleteBtn.addEventListener("click", async function (e) {
            e.preventDefault();
            if (!confirm("이미지를 삭제하시겠습니까?")) return;

            try {
                const res = await fetch("/api/mypage/profile/image", {
                    method: "DELETE",
                    headers: buildHeaders(),
                    credentials: "same-origin"
                });
                if (!res.ok) throw new Error("삭제 실패 " + res.status);

                // 성공: 기본 이미지로 교체 + 파일 입력 초기화
                if (previewImage) previewImage.src = "/img/진욱.svg";
                if (fileInput) fileInput.value = "";
                setSubmitEnabled(false);
                alert("이미지가 삭제되었습니다.");
            } catch (err) {
                console.error(err);
                alert("삭제 요청 중 오류가 발생했습니다.");
            }
        });
    }

    // 초기 상태(안전)
    setSubmitEnabled(false);
});
