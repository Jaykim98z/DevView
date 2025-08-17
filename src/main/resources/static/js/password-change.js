/**
 * 비밀번호 변경 모달 관리 클래스
 * 회원가입의 비밀번호 강도 검증 로직 재사용
 */
class PasswordChangeModal {
    constructor() {
        this.modal = null;
        this.form = null;
        this.inputs = {};
        this.validation = {
            currentPassword: false,
            newPassword: false,
            newPasswordConfirm: false
        };

        this.csrfToken = null;
        this.csrfHeader = null;

        this.init();
    }

    init() {
        this.setupElements();
        this.setupCSRF();
        this.bindEvents();
    }

    setupElements() {
        // 모달 관련 요소들
        this.modal = document.getElementById('passwordChangeModal');
        this.form = document.getElementById('passwordChangeForm');

        // 버튼들
        this.changePasswordBtn = document.getElementById('changePasswordBtn');
        this.closeModalBtn = document.getElementById('closeModalBtn');
        this.cancelBtn = document.getElementById('cancelBtn');
        this.submitBtn = document.getElementById('submitBtn');

        // 입력 필드들
        this.inputs = {
            currentPassword: document.getElementById('currentPassword'),
            newPassword: document.getElementById('newPassword'),
            newPasswordConfirm: document.getElementById('newPasswordConfirm')
        };

        // 비밀번호 토글 버튼들
        this.passwordToggles = document.querySelectorAll('.password-toggle');
    }

    setupCSRF() {
        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

        this.csrfToken = csrfTokenMeta ? csrfTokenMeta.content : null;
        this.csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : null;

        if (!this.csrfToken || !this.csrfHeader) {
            console.warn('CSRF 토큰이 페이지에 없습니다.');
        }
    }

    bindEvents() {
        // 모달 열기/닫기 이벤트
        if (this.changePasswordBtn) {
            this.changePasswordBtn.addEventListener('click', () => this.openModal());
        }

        if (this.closeModalBtn) {
            this.closeModalBtn.addEventListener('click', () => this.closeModal());
        }

        if (this.cancelBtn) {
            this.cancelBtn.addEventListener('click', () => this.closeModal());
        }

        // 모달 외부 클릭 시 닫기
        if (this.modal) {
            this.modal.addEventListener('click', (e) => {
                if (e.target === this.modal) {
                    this.closeModal();
                }
            });
        }

        // ESC 키로 모달 닫기
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.modal && this.modal.classList.contains('show')) {
                this.closeModal();
            }
        });

        // 폼 제출 이벤트
        if (this.form) {
            this.form.addEventListener('submit', (e) => this.handleSubmit(e));
        }

        // 입력 필드 이벤트
        if (this.inputs.currentPassword) {
            this.inputs.currentPassword.addEventListener('input', () => this.handleCurrentPasswordInput());
        }

        if (this.inputs.newPassword) {
            this.inputs.newPassword.addEventListener('input', () => this.handleNewPasswordInput());
        }

        if (this.inputs.newPasswordConfirm) {
            this.inputs.newPasswordConfirm.addEventListener('input', () => this.handleNewPasswordConfirmInput());
        }

        // 비밀번호 토글 이벤트
        this.passwordToggles.forEach(toggle => {
            toggle.addEventListener('click', () => this.togglePasswordVisibility(toggle));
        });
    }

    openModal() {
        if (this.modal) {
            this.modal.classList.add('show');
            document.body.style.overflow = 'hidden';

            // 첫 번째 입력 필드에 포커스
            if (this.inputs.currentPassword) {
                setTimeout(() => this.inputs.currentPassword.focus(), 100);
            }
        }
    }

    closeModal() {
        if (this.modal) {
            this.modal.classList.remove('show');
            document.body.style.overflow = '';

            // 폼 초기화
            this.resetForm();
        }
    }

    resetForm() {
        if (this.form) {
            this.form.reset();
        }

        // 검증 상태 초기화
        this.validation = {
            currentPassword: false,
            newPassword: false,
            newPasswordConfirm: false
        };

        // 에러/성공 메시지 숨김
        this.hideAllMessages();

        // 입력 필드 클래스 제거
        Object.values(this.inputs).forEach(input => {
            if (input) {
                input.classList.remove('error', 'success');
            }
        });

        // 비밀번호 강도 초기화
        this.resetPasswordStrength();

        // 제출 버튼 비활성화
        this.updateSubmitButton();
    }

    togglePasswordVisibility(button) {
        const targetId = button.getAttribute('data-target');
        const input = document.getElementById(targetId);
        const icon = button.querySelector('i');

        if (input && icon) {
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
    }

    handleCurrentPasswordInput() {
        const currentPassword = this.inputs.currentPassword.value;

        if (currentPassword.length > 0) {
            this.validation.currentPassword = true;
            this.hideMessage('currentPasswordError');
            this.inputs.currentPassword.classList.remove('error');
        } else {
            this.validation.currentPassword = false;
        }

        this.updateSubmitButton();
    }

    handleNewPasswordInput() {
        const password = this.inputs.newPassword.value;
        const strength = this.calculatePasswordStrength(password);

        this.updatePasswordStrength(strength);
        this.validation.newPassword = strength.isValid;

        if (password.length > 0 && !strength.isValid) {
            this.showError('newPasswordError', '영어+숫자+특수문자 조합 8~20자리로 입력해주세요.');
            this.inputs.newPassword.classList.add('error');
            this.inputs.newPassword.classList.remove('success');
        } else if (strength.isValid) {
            this.hideMessage('newPasswordError');
            this.inputs.newPassword.classList.remove('error');
            this.inputs.newPassword.classList.add('success');
        } else {
            this.hideMessage('newPasswordError');
            this.inputs.newPassword.classList.remove('error', 'success');
        }

        // 새 비밀번호 확인 재검증
        if (this.inputs.newPasswordConfirm.value) {
            this.handleNewPasswordConfirmInput();
        }

        this.updateSubmitButton();
    }

    handleNewPasswordConfirmInput() {
        const newPassword = this.inputs.newPassword.value;
        const newPasswordConfirm = this.inputs.newPasswordConfirm.value;

        if (newPasswordConfirm.length === 0) {
            this.validation.newPasswordConfirm = false;
            this.hideMessage('newPasswordConfirmError');
            this.hideMessage('newPasswordConfirmSuccess');
            this.inputs.newPasswordConfirm.classList.remove('error', 'success');
        } else if (newPassword !== newPasswordConfirm) {
            this.validation.newPasswordConfirm = false;
            this.showError('newPasswordConfirmError', '새 비밀번호가 일치하지 않습니다.');
            this.inputs.newPasswordConfirm.classList.add('error');
            this.inputs.newPasswordConfirm.classList.remove('success');
            this.hideMessage('newPasswordConfirmSuccess');
        } else {
            this.validation.newPasswordConfirm = true;
            this.hideMessage('newPasswordConfirmError');
            this.showSuccess('newPasswordConfirmSuccess', '새 비밀번호가 일치합니다.');
            this.inputs.newPasswordConfirm.classList.remove('error');
            this.inputs.newPasswordConfirm.classList.add('success');
        }

        this.updateSubmitButton();
    }

    /**
     * 비밀번호 강도 계산 (회원가입에서 가져온 로직)
     */
    calculatePasswordStrength(password) {
        let score = 0;

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

        if (strengthText && strengthFill) {
            // 텍스트 업데이트
            const textMap = {
                weak: '약함',
                medium: '보통',
                strong: '강함'
            };

            strengthText.textContent = textMap[strength.level];

            // 바 업데이트
            strengthFill.className = `strength-fill ${strength.level}`;
        }
    }

    resetPasswordStrength() {
        const strengthText = document.querySelector('.strength-text');
        const strengthFill = document.getElementById('strengthFill');

        if (strengthText && strengthFill) {
            strengthText.textContent = '보통';
            strengthFill.className = 'strength-fill';
        }
    }

    async handleSubmit(e) {
        e.preventDefault();

        const currentPassword = this.inputs.currentPassword.value.trim();
        const newPassword = this.inputs.newPassword.value;
        const newPasswordConfirm = this.inputs.newPasswordConfirm.value;

        // 최종 검증
        if (!this.isFormValid()) {
            alert('모든 필드를 올바르게 입력해주세요.');
            return;
        }

        // 같은 비밀번호 체크
        if (currentPassword === newPassword) {
            this.showError('newPasswordError', '새 비밀번호는 현재 비밀번호와 달라야 합니다.');
            return;
        }

        this.showLoading();

        try {
            const headers = {
                'Content-Type': 'application/json',
            };

            // CSRF 토큰 추가
            if (this.csrfToken && this.csrfHeader) {
                headers[this.csrfHeader] = this.csrfToken;
            }

            const response = await fetch('/api/users/password', {
                method: 'PUT',
                headers: headers,
                body: JSON.stringify({
                    currentPassword: currentPassword,
                    newPassword: newPassword,
                    newPasswordConfirm: newPasswordConfirm
                }),
                credentials: 'same-origin'
            });

            if (response.ok) {
                const result = await response.json();
                alert('비밀번호가 성공적으로 변경되었습니다.');
                this.closeModal();
            } else {
                const errorData = await response.json().catch(() => ({}));
                const message = errorData.message || '비밀번호 변경에 실패했습니다.';

                if (response.status === 400 && message.includes('현재 비밀번호')) {
                    this.showError('currentPasswordError', message);
                } else {
                    alert(message);
                }
            }
        } catch (error) {
            console.error('비밀번호 변경 오류:', error);
            alert('비밀번호 변경 중 오류가 발생했습니다. 다시 시도해주세요.');
        } finally {
            this.hideLoading();
        }
    }

    updateSubmitButton() {
        const isValid = this.isFormValid();
        if (this.submitBtn) {
            this.submitBtn.disabled = !isValid;
        }
    }

    isFormValid() {
        return Object.values(this.validation).every(valid => valid === true);
    }

    showError(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = message;
            element.classList.add('show');
        }
    }

    showSuccess(elementId, message) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = message;
            element.classList.add('show');
        }
    }

    hideMessage(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.remove('show');
        }
    }

    hideAllMessages() {
        const messages = document.querySelectorAll('.error-message, .success-message');
        messages.forEach(message => message.classList.remove('show'));
    }

    showLoading() {
        if (this.submitBtn) {
            this.submitBtn.textContent = '변경 중...';
            this.submitBtn.disabled = true;
        }
    }

    hideLoading() {
        if (this.submitBtn) {
            this.submitBtn.textContent = '비밀번호 변경';
            this.submitBtn.disabled = !this.isFormValid();
        }
    }
}

// DOM이 로드되면 초기화
document.addEventListener('DOMContentLoaded', () => {
    new PasswordChangeModal();
});