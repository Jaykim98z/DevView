document.addEventListener("DOMContentLoaded", function () {
    // ===== DOM refs (HTML과 정확히 매칭) =====
    const profileForm     = document.getElementById("profileForm");        // 변경사항 저장 폼
    const imageUploadForm = document.getElementById("imageUploadForm");    // 드래그&드랍 영역(실제 submit 안 함)
    const deleteImageForm = document.getElementById("deleteImageForm");    // 삭제 폼

    const imageInput   = document.getElementById("imageInput");            // 파일 input
    const imagePreview = document.getElementById("imagePreview");          // 미리보기 <img>
    const uploadTrigger = document.getElementById("uploadTrigger");        // 업로드 버튼
    const dropArea     = document.getElementById("dropArea");              // 드래그&드랍 영역

    // 프로필 입력값
    const nameInput         = document.getElementById("name");
    const jobSelect         = document.getElementById("job");
    const careerLevelSelect = document.getElementById("careerLevel");

    // ===== 상수 =====
    const MAX = 5 * 1024 * 1024; // 5MB
    const OK_TYPES = ["image/jpeg", "image/jpg", "image/png"];
    const REDIRECT_TO = "/mypage"; // 저장 성공 시 이동할 경로

    // 브라우저가 파일 드롭 시 바로 열어버리는 기본 동작 방지
    ["dragover", "drop"].forEach(evt => {
        window.addEventListener(evt, e => e.preventDefault());
    });

    // ===== 공통 유틸 =====
    function buildHeaders(base = {}) {
        const headers = new Headers(base);

        // JWT (있다면)
        const getAccessToken = (typeof window !== "undefined") ? window["getAccessToken"] : undefined;
        if (typeof getAccessToken === "function") {
            const token = getAccessToken();
            if (token) headers.set("Authorization", "Bearer " + token);
        }

        // CSRF: meta 우선, 없으면 hidden input
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

    async function fetchJson(url, options = {}) {
        const headers = buildHeaders(options.headers || {});
        let res, text = "", data = null;

        try {
            res = await fetch(url, { ...options, headers, credentials: "same-origin" });
        } catch (e) {
            console.error(e);
            alert("네트워크 오류가 발생했습니다. 인터넷 연결을 확인해 주세요.");
            return null;
        }

        try {
            text = await res.text(); // 먼저 텍스트로 받고
            data = text ? JSON.parse(text) : null; // 가능하면 JSON으로 변환
        } catch {
            data = null; // JSON 아님
        }

        if (!res.ok) {
            // 상태코드별 가이드
            let friendly = "";
            switch (res.status) {
                case 400: friendly = "입력값을 다시 확인해 주세요 (400)."; break;
                case 401: friendly = "로그인이 필요합니다 (401)."; break;
                case 403: friendly = "권한/CSRF 문제가 있습니다 (403). 새로고침 후 다시 시도해 주세요."; break;
                case 404: friendly = "요청 경로가 없습니다 (404)."; break;
                case 413: friendly = "파일이 너무 큽니다 (413). 5MB 이하 이미지를 올려주세요."; break;
                case 415: friendly = "지원하지 않는 콘텐츠 형식입니다 (415)."; break;
                default:  friendly = `요청 실패 (${res.status}).`;
            }
            const serverMsg =
                (data && (data.message || data.error || data.detail)) ||
                text ||
                "";
            console.error("API ERROR", { status: res.status, body: serverMsg });
            alert(`${friendly}\n\n서버 응답: ${serverMsg.substring(0, 500)}`);
            return null;
        }
        return data ?? {};
    }

    function toProfilePayload() {
        return {
            name: (nameInput?.value ?? "").trim(),
            job: (jobSelect?.value ?? "").trim(),
            careerLevel: (careerLevelSelect?.value ?? "").trim()
            // 확장 필드가 있으면 여기에 추가 (bio, location, skills 등)
        };
    }

    // ===== 이미지 유틸 =====
    function validateImage(file) {
        if (!file) return false;
        if (!OK_TYPES.includes(file.type)) {
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
        if (!file || !imagePreview) return;
        const reader = new FileReader();
        reader.onload = e => { imagePreview.src = e.target.result; };
        reader.readAsDataURL(file);
    }

    function assignFileToInput(file) {
        if (!imageInput || !file) return;
        try {
            const dt = new DataTransfer();
            dt.items.add(file);
            imageInput.files = dt.files;
        } catch { /* fallback 무시 */ }
    }

    // ===== 파일 선택/드래그앤드랍 UI =====
    if (uploadTrigger && imageInput) {
        uploadTrigger.addEventListener("click", () => imageInput.click());
    }

    if (imageInput) {
        imageInput.addEventListener("change", () => {
            const file = imageInput.files?.[0];
            if (!file) return;
            if (!validateImage(file)) {
                imageUploadForm?.reset();
                return;
            }
            preview(file);
        });
    }

    if (dropArea && imageInput) {
        ["dragenter", "dragover"].forEach(evt =>
            dropArea.addEventListener(evt, e => {
                e.preventDefault();
                dropArea.classList.add("dragover");
            })
        );
        ["dragleave", "drop"].forEach(evt =>
            dropArea.addEventListener(evt, e => {
                e.preventDefault();
                dropArea.classList.remove("dragover");
            })
        );
        dropArea.addEventListener("drop", e => {
            const file = e.dataTransfer?.files?.[0];
            if (!file) return;
            if (!validateImage(file)) return;
            assignFileToInput(file);
            preview(file);
        });

        // 키보드 접근성
        dropArea.addEventListener("keydown", e => {
            if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                imageInput?.click();
            }
        });
        dropArea.addEventListener("click", e => {
            if (e.target === dropArea) imageInput?.click();
        });
    }

    // ===== 변경사항 저장: profileForm submit 가로채서 API 호출 + 성공 시 이동 =====
    if (profileForm) {
        profileForm.addEventListener("submit", async e => {
            e.preventDefault();

            const profile = toProfilePayload();
            const hasImage = !!(imageInput && imageInput.files && imageInput.files.length > 0);

            if (hasImage) {
                // POST /api/mypage/profile (multipart)
                const fd = new FormData();
                fd.append("profile", new Blob([JSON.stringify(profile)], { type: "application/json" }));
                fd.append("profileImage", imageInput.files[0]);

                const headers = buildHeaders(); // Content-Type 자동 세팅 위해 수동 지정 금지
                let res, data = {};
                try {
                    res = await fetch("/api/mypage/profile", {
                        method: "POST",
                        headers,
                        body: fd,
                        credentials: "same-origin"
                    });
                    try { data = await res.json(); } catch { /* ignore */ }
                } catch (err) {
                    console.error(err);
                    alert("네트워크 오류가 발생했습니다.");
                    return;
                }
                if (!res.ok) {
                    alert((data && data.message) || `업로드 실패 (${res.status})`);
                    return;
                }

                // 성공 시 미리보기 갱신(응답에 URL이 있을 경우)
                const newUrl = (data && typeof data === "object") ? data["profileImageUrl"] : null;
                if (newUrl && imagePreview) imagePreview.src = newUrl;

                alert("프로필이 저장되었습니다.");
                // ✅ 저장 성공 → 마이페이지로 이동
                window.location.assign(REDIRECT_TO);
            } else {
                // PUT /api/mypage/profile (JSON)
                const data = await fetchJson("/api/mypage/profile", {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(profile)
                });
                if (!data) return; // fetchJson이 이미 alert 처리

                alert("프로필이 저장되었습니다.");
                // ✅ 저장 성공 → 마이페이지로 이동
                window.location.assign(REDIRECT_TO);
            }
        });
    }

    // ===== 이미지 삭제: deleteImageForm submit 가로채서 DELETE 호출 =====
    if (deleteImageForm) {
        deleteImageForm.addEventListener("submit", async e => {
            e.preventDefault();
            if (!confirm("이미지를 삭제하시겠습니까?")) return;

            const headers = buildHeaders();
            let res, data = {};
            try {
                res = await fetch("/api/mypage/profile/image", {
                    method: "DELETE",
                    headers,
                    credentials: "same-origin"
                });
                try { data = await res.json(); } catch { /* ignore */ }
            } catch (err) {
                console.error(err);
                alert("네트워크 오류가 발생했습니다.");
                return;
            }
            if (!res.ok) {
                alert((data && data.message) || `삭제 실패 (${res.status})`);
                return;
            }

            // 성공: 기본 이미지로 교체 + 파일 입력 초기화
            if (imagePreview) imagePreview.src = "/img/진욱.svg";
            if (imageInput) imageInput.value = "";
            alert("이미지가 삭제되었습니다.");
        });
    }
});
