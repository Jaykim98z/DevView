/**
 * 로그인 폼 관리 클래스
 */
class LoginForm {
    constructor() {
        this.initElements();
        this.attachEventListeners();
        this.initCsrfToken();
    }

    initElements() {
        // 폼 요소들
        this.form = document.getElementById('loginForm');
        this.inputs = {
            email: document.getElementById('email'),
            password: document.getElementById('password')
        };
        this.buttons = {
            submit: document.getElementById('loginSubmitBtn'),
            googleLogin: document.getElementById('googleLoginBtn'),
            passwordToggle: document.getElementById('passwordToggle')
        };
    }

    initCsrfToken() {
        // CSRF 토큰 정보 저장
        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

        this.csrfToken = csrfTokenMeta ? csrfTokenMeta.content : null;
        this.csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : null;

        if (!this.csrfToken || !this.csrfHeader) {
            console.warn('CSRF 토큰이 페이지에 없습니다. CSRF 보호가 비활성화되어 있을 수 있습니다.');
        }
    }

    attachEventListeners() {
        // 폼 제출
        this.form.addEventListener('submit', (e) => this.handleSubmit(e));

        // 입력 필드 이벤트
        this.inputs.email.addEventListener('input', () => this.handleEmailInput());
        this.inputs.password.addEventListener('input', () => this.handlePasswordInput());

        // 버튼 이벤트
        this.buttons.googleLogin.addEventListener('click', () => this.handleGoogleLogin());
        this.buttons.passwordToggle.addEventListener('click', () => {
            this.togglePasswordVisibility(this.inputs.password, this.buttons.passwordToggle);
        });

        // Enter 키 처리
        this.inputs.email.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !this.buttons.submit.disabled) {
                this.form.dispatchEvent(new Event('submit'));
            }
        });
        this.inputs.password.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !this.buttons.submit.disabled) {
                this.form.dispatchEvent(new Event('submit'));
            }
        });
    }

    togglePasswordVisibility(input, button) {
        const icon = button.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    }

    handleEmailInput() {
        const email = this.inputs.email.value.trim();

        if (email.length > 0 && !this.validateEmail(email)) {
            this.showError('emailError', '올바른 이메일 형식으로 입력해주세요.');
            this.inputs.email.classList.add('error');
        } else {
            this.hideMessage('emailError');
            this.inputs.email.classList.remove('error');
        }

        this.updateSubmitButton();
    }

    handlePasswordInput() {
        const password = this.inputs.password.value;

        if (password.length > 0 && password.length < 8) {
            this.showError('passwordError', '비밀번호는 8자 이상이어야 합니다.');
            this.inputs.password.classList.add('error');
        } else {
            this.hideMessage('passwordError');
            this.inputs.password.classList.remove('error');
        }

        this.updateSubmitButton();
    }

    async handleSubmit(e) {
        e.preventDefault();

        const email = this.inputs.email.value.trim();
        const password = this.inputs.password.value;

        // 기본 유효성 검사
        if (!email || !password) {
            alert('이메일과 비밀번호를 모두 입력해주세요.');
            return;
        }

        if (!this.validateEmail(email)) {
            alert('올바른 이메일 형식으로 입력해주세요.');
            this.inputs.email.focus();
            return;
        }

        // FormData로 전송 (CSRF 토큰 포함)
        const formData = new URLSearchParams();
        formData.append('email', email);
        formData.append('password', password);

        // CSRF 토큰 추가
        if (this.csrfToken) {
            formData.append('_csrf', this.csrfToken);
        }

        this.showLoading();

        try {
            // Spring Security 폼 로그인 엔드포인트로 전송
            const headers = {
                'Content-Type': 'application/x-www-form-urlencoded',
            };

            // AJAX 요청 헤더에도 CSRF 토큰 추가 (이중 보안)
            if (this.csrfToken && this.csrfHeader) {
                headers[this.csrfHeader] = this.csrfToken;
            }

            const response = await fetch('/api/users/login', {
                method: 'POST',
                headers: headers,
                body: formData,
                credentials: 'same-origin'  // 세션 쿠키 포함
            });

            if (response.ok) {
                const result = await response.json();

                if (result.success) {
                    // 로그인 성공
                    console.log('로그인 성공:', result.email);

                    // 메인 페이지로 리다이렉트
                    window.location.href = '/';
                } else {
                    // 로그인 실패
                    alert(result.message || '로그인에 실패했습니다.');
                    this.inputs.password.value = '';
                    this.inputs.password.focus();
                }
            } else if (response.status === 403) {
                // CSRF 토큰 문제
                console.error('CSRF 토큰 오류');
                alert('보안 토큰이 만료되었습니다. 페이지를 새로고침 해주세요.');
                location.reload();
            } else {
                const errorData = await response.json();
                alert(errorData.message || '이메일 또는 비밀번호가 올바르지 않습니다.');

                // 비밀번호 필드 초기화
                this.inputs.password.value = '';
                this.inputs.password.focus();
            }
        } catch (error) {
            console.error('로그인 오류:', error);
            alert('로그인 중 오류가 발생했습니다. 네트워크 연결을 확인해주세요.');
        } finally {
            this.hideLoading();
        }
    }

    async handleGoogleLogin() {
        // Google OAuth2 로그인 시작
        try {
            console.log('Google OAuth2 로그인 시작');

            // Spring Security OAuth2 엔드포인트로 리다이렉트
            window.location.href = '/oauth2/authorization/google';

        } catch (error) {
            console.error('Google 로그인 오류:', error);
            alert('Google 로그인 중 오류가 발생했습니다.');
        }
    }

    validateEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    updateSubmitButton() {
        const email = this.inputs.email.value.trim();
        const password = this.inputs.password.value;

        const isValid = email.length > 0 && password.length > 0 && this.validateEmail(email);
        this.buttons.submit.disabled = !isValid;
    }

    showError(elementId, message) {
        const element = document.getElementById(elementId);
        element.textContent = message;
        element.classList.add('show');
    }

    hideMessage(elementId) {
        const element = document.getElementById(elementId);
        element.classList.remove('show');
    }

    showLoading() {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.classList.add('show');
        }
    }

    hideLoading() {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.classList.remove('show');
        }
    }
}

// DOM이 로드되면 초기화
document.addEventListener('DOMContentLoaded', () => {
    new LoginForm();
});