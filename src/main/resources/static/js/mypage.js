document.addEventListener("DOMContentLoaded", function () {
    console.log('mypage.js 로드됨'); // 디버깅용

    // 1) 점수 변화 라인차트
    const scoreCtx = document.getElementById("scoreChart");
    if (scoreCtx) {
        let labels = [];
        let scores = [];
        try {
            labels = JSON.parse(scoreCtx.dataset.labels || '[]');
            scores = JSON.parse(scoreCtx.dataset.scores || '[]');
        } catch (e) {
            console.error('그래프 데이터 파싱 오류', e);
        }

        // 혹시 문자열 숫자가 섞여 있으면 숫자로 보정
        scores = (scores || []).map(n => typeof n === 'string' ? Number(n) : n);

        if (typeof Chart !== 'undefined') {
            new Chart(scoreCtx, {
                type: 'line',
                data: {
                    labels: labels.length > 0 ? labels : ["데이터 없음"],
                    datasets: [{
                        label: '면접 점수',
                        data: scores.length > 0 ? scores : [0],
                        borderColor: '#4AB2E3',
                        backgroundColor: 'rgba(74, 178, 227, 0.1)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0.3,
                        pointBackgroundColor: '#4AB2E3',
                        pointRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false }, title: { display: false } },
                    scales: { y: { beginAtZero: true, max: 100, ticks: { stepSize: 20 } } }
                }
            });
        } else {
            console.error('Chart.js가 로드되지 않았습니다.');
        }
    }

    // 2) 관심 직무 도넛차트
    const careerCtx = document.getElementById("careerChart");
    if (careerCtx) {
        let labels = [];
        let data = [];
        try {
            labels = JSON.parse(careerCtx.dataset.labels || '[]');
            data = JSON.parse(careerCtx.dataset.data || '[]');
        } catch (e) {
            console.error('관심 직무 데이터 파싱 오류', e);
        }

        if (typeof Chart !== 'undefined') {
            new Chart(careerCtx, {
                type: 'doughnut',
                data: {
                    labels: labels.length > 0 ? labels : ["데이터 없음"],
                    datasets: [{
                        label: '관심 직무',
                        data: data.length > 0 ? data : [1],
                        backgroundColor: [
                            'rgba(255, 99, 132, 0.6)',
                            'rgba(54, 162, 235, 0.6)',
                            'rgba(255, 206, 86, 0.6)',
                            'rgba(75, 192, 192, 0.6)',
                            'rgba(153, 102, 255, 0.6)',
                            'rgba(255, 159, 64, 0.6)'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { position: 'bottom' }, title: { display: false } }
                }
            });
        }
    }

    // 3) 회원탈퇴 모달 기능
    initWithdrawalModal();
});

// ===== 회원탈퇴 모달 관련 함수들 =====

function initWithdrawalModal() {
    console.log('회원탈퇴 모달 초기화 시작'); // 디버깅용

    // 탈퇴 폼 찾기 - 여러 방법으로 시도
    const withdrawalForm = document.querySelector('#withdrawalForm') ||
                          document.querySelector('form[action="/mypage/delete"]');

    if (withdrawalForm) {
        console.log('탈퇴 폼 찾음:', withdrawalForm); // 디버깅용

        // 기존 onsubmit 속성 제거
        withdrawalForm.removeAttribute('onsubmit');

        // 새로운 이벤트 리스너 추가
        withdrawalForm.addEventListener('submit', function(e) {
            console.log('탈퇴 버튼 클릭됨'); // 디버깅용
            e.preventDefault(); // 기본 제출 방지

            // 커스텀 확인 모달 표시
            showWithdrawalConfirmModal(withdrawalForm);
        });
    } else {
        console.error('탈퇴 폼을 찾을 수 없습니다');
    }
}

/**
 * 회원탈퇴 확인 모달 표시
 * @param {HTMLFormElement} form 제출할 폼 엘리먼트
 */
function showWithdrawalConfirmModal(form) {
    console.log('모달 표시'); // 디버깅용

    // 모달 HTML 생성
    const modalHTML = `
        <div id="withdrawalModal" class="withdrawal-modal-overlay">
            <div class="withdrawal-modal">
                <div class="modal-header">
                    <h3>회원탈퇴 확인</h3>
                </div>
                <div class="modal-body">
                    <p><strong>정말로 탈퇴하시겠습니까?</strong></p>
                    <p>탈퇴 시 다음 데이터가 모두 삭제됩니다:</p>
                    <ul>
                        <li>프로필 정보</li>
                        <li>면접 기록 및 결과</li>
                        <li>커뮤니티 글 및 댓글</li>
                        <li>랭킹 정보</li>
                    </ul>
                    <p class="warning">⚠️ 이 작업은 되돌릴 수 없습니다.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="modal-btn cancel-btn" onclick="closeWithdrawalModal()">
                        취소
                    </button>
                    <button type="button" class="modal-btn confirm-btn" onclick="confirmWithdrawal()">
                        탈퇴하기
                    </button>
                </div>
            </div>
        </div>
    `;

    // CSS 스타일 추가 (한 번만)
    if (!document.getElementById('withdrawalModalStyles')) {
        const styles = `
            <style id="withdrawalModalStyles">
                .withdrawal-modal-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0, 0, 0, 0.5);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    z-index: 10000;
                }

                .withdrawal-modal {
                    background: white;
                    border-radius: 12px;
                    width: 90%;
                    max-width: 450px;
                    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
                    animation: modalAppear 0.2s ease-out;
                }

                @keyframes modalAppear {
                    from {
                        opacity: 0;
                        transform: scale(0.9) translateY(-20px);
                    }
                    to {
                        opacity: 1;
                        transform: scale(1) translateY(0);
                    }
                }

                .modal-header {
                    padding: 20px 24px 0;
                    border-bottom: none;
                }

                .modal-header h3 {
                    margin: 0;
                    font-size: 1.25rem;
                    font-weight: 600;
                    color: #dc3545;
                }

                .modal-body {
                    padding: 16px 24px;
                }

                .modal-body p {
                    margin: 0 0 12px 0;
                    line-height: 1.5;
                }

                .modal-body ul {
                    margin: 12px 0;
                    padding-left: 20px;
                }

                .modal-body li {
                    margin: 4px 0;
                    color: #666;
                }

                .modal-body .warning {
                    color: #dc3545;
                    font-weight: 500;
                    background: #fff5f5;
                    padding: 8px 12px;
                    border-radius: 6px;
                    border-left: 4px solid #dc3545;
                }

                .modal-footer {
                    padding: 16px 24px 24px;
                    display: flex;
                    gap: 12px;
                    justify-content: flex-end;
                }

                .modal-btn {
                    padding: 10px 20px;
                    border-radius: 8px;
                    font-size: 0.9rem;
                    font-weight: 500;
                    cursor: pointer;
                    transition: all 0.2s;
                    border: none;
                }

                .cancel-btn {
                    background: #f8f9fa;
                    color: #495057;
                    border: 1px solid #dee2e6;
                }

                .cancel-btn:hover {
                    background: #e9ecef;
                }

                .confirm-btn {
                    background: #dc3545;
                    color: white;
                }

                .confirm-btn:hover {
                    background: #c82333;
                }
            </style>
        `;
        document.head.insertAdjacentHTML('beforeend', styles);
    }

    // 모달을 body에 추가
    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // 폼 엘리먼트를 전역 변수에 저장
    window.withdrawalForm = form;

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', handleModalKeydown);
}

/**
 * 모달 닫기
 */
function closeWithdrawalModal() {
    console.log('모달 닫기'); // 디버깅용
    const modal = document.getElementById('withdrawalModal');
    if (modal) {
        modal.remove();
    }
    window.withdrawalForm = null;
    document.removeEventListener('keydown', handleModalKeydown);
}

/**
 * 탈퇴 확인
 */
function confirmWithdrawal() {
    console.log('탈퇴 확인 버튼 클릭'); // 디버깅용

    const form = window.withdrawalForm;
    closeWithdrawalModal();

    if (form) {
        console.log('폼 제출 시작:', form); // 디버깅용
        form.submit();
    } else {
        console.error('폼을 찾을 수 없습니다');
    }
}
