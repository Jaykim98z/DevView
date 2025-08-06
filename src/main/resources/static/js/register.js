// 회원가입 페이지 JavaScript

class RegisterForm {
    constructor() {
        this.form = document.getElementById('registerForm');
        this.inputs = {
            username: document.getElementById('username'),
            password: document.getElementById('password'),
            passwordConfirm: document.getElementById('passwordConfirm'),
            email: document.getElementById('email')
        };

        this.buttons = {
            usernameCheck: document.getElementById('usernameCheckBtn'),
            emailCheck: document.getElementById('emailCheckBtn'),
            submit: document.getElementById('registerSubmitBtn'),
            passwordToggle: document.getElementById('passwordToggle'),
            passwordConfirmToggle: document.getElementById('passwordConfirmToggle')
        };

        this.validation = {
            username: false,
            password: false,
            passwordConfirm: false,
            email: false
        };

        this.init();
    }

    init() {
        this.bindEvents();
        this.setupPasswordToggles();
    }

    bindEvents() {
        // 폼 제출 이벤트
        this.form.addEventListener('submit', (e) => this.handleSubmit(e));

        // 입력 필드 이벤트
        this.inputs.username.addEventListener('input', () => this.handleUsernameInput());
        this.inputs.password.addEventListener('input', () => this.handlePasswordInput());
        this.inputs.passwordConfirm.addEventListener('input', () => this.handlePasswordConfirmInput());
        this.inputs.email.addEventListener('input', () => this.handleEmailInput());

        // 중복 확인 버튼 이벤트
        this.buttons.usernameCheck.addEventListener('click', () => this.checkUsernameAvailability());
        this.buttons.emailCheck.addEventListener('click', () => this.checkEmailAvailability());
    }

    setupPasswordToggles() {
        // 비밀번호 보기/숨기기 토글
        this.buttons.passwordToggle.addEventListener('click', () => {
            this.togglePasswordVisibility(this.inputs.password, this.buttons.passwordToggle);
        });

        this.buttons.passwordConfirmToggle.addEventListener('click', () => {
            this.togglePasswordVisibility(this.inputs.passwordConfirm, this.buttons.passwordConfirmToggle);
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

    handleUsernameInput() {
        const username = this.inputs.username.value.trim();
        const isValid = this.validateUsername(username);

        this.validation.username = false;
        this.buttons.usernameCheck.disabled = !isValid;
        this.buttons.usernameCheck.classList.remove('success');
        this.hideMessage('usernameSuccess');

        if (username.length > 0 && !isValid) {
            this.showError('usernameError', '2~10자리로 입력해주세요.');
            this.inputs.username.classList.add('error');
            this.inputs.username.classList.remove('success');
        } else {
            this.hideMessage('usernameError');
            this.inputs.username.classList.remove('error');
        }

        this.updateSubmitButton();
    }

    handlePasswordInput() {
        const password = this.inputs.password.value;
        const strength = this.calculatePasswordStrength(password);

        this.updatePasswordStrength(strength);
        this.validation.password = strength.isValid;

        if (password.length > 0 && !strength.isValid) {
            this.showError('passwordError', '영어+숫자+특수문자 조합 8~20자리로 입력해주세요.');
            this.inputs.password.classList.add('error');
            this.inputs.password.classList.remove('success');
        } else if (strength.isValid) {
            this.hideMessage('passwordError');
            this.inputs.password.classList.remove('error');
            this.inputs.password.classList.add('success');
        } else {
            this.hideMessage('passwordError');
            this.inputs.password.classList.remove('error', 'success');
        }

        // 비밀번호 확인 재검증
        if (this.inputs.passwordConfirm.value) {
            this.handlePasswordConfirmInput();
        }

        this.updateSubmitButton();
    }

    handlePasswordConfirmInput() {
        const password = this.inputs.password.value;
        const passwordConfirm = this.inputs.passwordConfirm.value;

        if (passwordConfirm.length === 0) {
            this.validation.passwordConfirm = false;
            this.hideMessage('passwordConfirmError');
            this.hideMessage('passwordConfirmSuccess');
            this.inputs.passwordConfirm.classList.remove('error', 'success');
        } else if (password !== passwordConfirm) {
            this.validation.passwordConfirm = false;
            this.showError('passwordConfirmError', '비밀번호가 일치하지 않습니다.');
            this.inputs.passwordConfirm.classList.add('error');
            this.inputs.passwordConfirm.classList.remove('success');
            this.hideMessage('passwordConfirmSuccess');
        } else {
            this.validation.passwordConfirm = true;
            this.hideMessage('passwordConfirmError');
            this.showSuccess('passwordConfirmSuccess', '비밀번호가 일치합니다.');
            this.inputs.passwordConfirm.classList.remove('error');
            this.inputs.passwordConfirm.classList.add('success');
        }

        this.updateSubmitButton();
    }

    handleEmailInput() {
        const email = this.inputs.email.value.trim();
        const isValid = this.validateEmail(email);

        this.validation.email = false;
        this.buttons.emailCheck.disabled = !isValid;
        this.buttons.emailCheck.classList.remove('success');
        this.hideMessage('emailSuccess');

        if (email.length > 0 && !isValid) {
            this.showError('emailError', '올바른 이메일 형식으로 입력해주세요.');
            this.inputs.email.classList.add('error');
            this.inputs.email.classList.remove('success');
        } else {
            this.hideMessage('emailError');
            this.inputs.email.classList.remove('error');
        }

        this.updateSubmitButton();
    }

    async checkUsernameAvailability() {
        const username = this.inputs.username.value.trim();
        if (!this.validateUsername(username)) return;

        this.buttons.usernameCheck.disabled = true;
        this.buttons.usernameCheck.textContent = '확인 중...';

        try {
            const response = await fetch(`/api/users/check-username?username=${encodeURIComponent(username)}`);
            const isAvailable = await response.json();

            if (isAvailable) {
                this.validation.username = true;
                this.showSuccess('usernameSuccess', '사용 가능한 이름입니다.');
                this.inputs.username.classList.remove('error');
                this.inputs.username.classList.add('success');
                this.buttons.usernameCheck.classList.add('success');
                this.buttons.usernameCheck.textContent = '확인 완료';
                this.hideMessage('usernameError');
            } else {
                this.validation.username = false;
                this.showError('usernameError', '이미 사용 중인 이름입니다.');
                this.inputs.username.classList.add('error');
                this.inputs.username.classList.remove('success');
                this.buttons.usernameCheck.textContent = '중복 확인';
                this.buttons.usernameCheck.disabled = false;
                this.hideMessage('usernameSuccess');
            }
        } catch (error) {
            console.error('이름 중복 확인 오류:', error);
            this.showError('usernameError', '중복 확인 중 오류가 발생했습니다.');
            this.buttons.usernameCheck.textContent = '중복 확인';
            this.buttons.usernameCheck.disabled = false;
        }

        this.updateSubmitButton();
    }

    async checkEmailAvailability() {
        const email = this.inputs.email.value.trim();
        if (!this.validateEmail(email)) return;

        this.buttons.emailCheck.disabled = true;
        this.buttons.emailCheck.textContent = '확인 중...';

        try {
            const response = await fetch(`/api/users/check-email?email=${encodeURIComponent(email)}`);
            const isAvailable = await response.json();

            if (isAvailable) {
                this.validation.email = true;
                this.showSuccess('emailSuccess', '사용 가능한 이메일입니다.');
                this.inputs.email.classList.remove('error');
                this.inputs.email.classList.add('success');
                this.buttons.emailCheck.classList.add('success');
                this.buttons.emailCheck.textContent = '확인 완료';
                this.hideMessage('emailError');
            } else {
                this.validation.email = false;
                this.showError('emailError', '이미 사용 중인 이메일입니다.');
                this.inputs.email.classList.add('error');
                this.inputs.email.classList.remove('success');
                this.buttons.emailCheck.textContent = '중복 확인';
                this.buttons.emailCheck.disabled = false;
                this.hideMessage('emailSuccess');
            }
        } catch (error) {
            console.error('이메일 중복 확인 오류:', error);
            this.showError('emailError', '중복 확인 중 오류가 발생했습니다.');
            this.buttons.emailCheck.textContent = '중복 확인';
            this.buttons.emailCheck.disabled = false;
        }

        this.updateSubmitButton();
    }

    async handleSubmit(e) {
        e.preventDefault();

        if (!this.isFormValid()) {
            alert('모든 필드를 올바르게 입력해주세요.');
            return;
        }

        const formData = {
            username: this.inputs.username.value.trim(),
            password: this.inputs.password.value,
            passwordConfirm: this.inputs.passwordConfirm.value,
            email: this.inputs.email.value.trim()
        };

        this.showLoading();

        try {
            const response = await fetch('/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                const userData = await response.json();
                alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
                window.location.href = '/user/login';
            } else {
                const errorData = await response.json();
                alert(errorData.message || '회원가입 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('회원가입 오류:', error);
            alert('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
        } finally {
            this.hideLoading();
        }
    }

    validateUsername(username) {
        // 한글, 영어, 숫자 조합 2~10자리
        const regex = /^[가-힣a-zA-Z0-9]{2,10}$/;
        return regex.test(username);
    }

    validateEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    calculatePasswordStrength(password) {
        let score = 0;
        let feedback = [];

        // 길이 체크
        if (password.length >= 8) score += 1;
        if (password.length >= 12) score += 1;

        // 문자 종류 체크
        if (/[a-z]/.test(password)) score += 1;
        if (/[A-Z]/.test(password)) score += 1;
        if (/[0-9]/.test(password)) score += 1;
        if (/[^a-zA-Z0-9]/.test(password)) score += 1;

        // 최소 요구사항 체크 (영어+숫자+특수문자, 8자 이상)
        const hasLetter = /[a-zA-Z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        const hasSpecial = /[^a-zA-Z0-9]/.test(password);
        const isLongEnough = password.length >= 8;
        const isNotTooLong = password.length <= 20;

        const isValid = hasLetter && hasNumber && hasSpecial && isLongEnough && isNotTooLong;

        let level;
        if (score <= 2) {
            level = 'weak';
        } else if (score <= 4) {
            level = 'medium';
        } else {
            level = 'strong';
        }

        return { score, level, isValid };
    }

    updatePasswordStrength(strength) {
        const strengthText = document.querySelector('.strength-text');
        const strengthFill = document.getElementById('strengthFill');

        // 텍스트 업데이트
        const textMap = {
            weak: '약',
            medium: '보통',
            strong: '강'
        };

        strengthText.textContent = textMap[strength.level];

        // 바 업데이트
        strengthFill.className = `strength-fill ${strength.level}`;
    }

    updateSubmitButton() {
        const isValid = this.isFormValid();
        this.buttons.submit.disabled = !isValid;
    }

    isFormValid() {
        return Object.values(this.validation).every(valid => valid === true);
    }

    showError(elementId, message) {
        const element = document.getElementById(elementId);
        element.textContent = message;
        element.classList.add('show');
    }

    showSuccess(elementId, message) {
        const element = document.getElementById(elementId);
        element.textContent = message;
        element.classList.add('show');
    }

    hideMessage(elementId) {
        const element = document.getElementById(elementId);
        element.classList.remove('show');
    }

    showLoading() {
        document.getElementById('loadingOverlay').classList.add('show');
    }

    hideLoading() {
        document.getElementById('loadingOverlay').classList.remove('show');
    }
}

// DOM이 로드되면 초기화
document.addEventListener('DOMContentLoaded', () => {
    new RegisterForm();
});