/**
 * 랭킹 페이지 JavaScript
 * 애니메이션 효과 및 인터랙션 처리
 */

class RankingPage {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.initAnimations();
    }

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        document.addEventListener('DOMContentLoaded', () => {
            this.showTopRankingsAnimation();
            this.showTableRowsAnimation();
            this.highlightMyRank();
        });

        // 테이블 행 호버 효과
        const tableRows = document.querySelectorAll('.ranking-table tbody tr');
        tableRows.forEach(row => {
            row.addEventListener('mouseenter', this.onRowHover.bind(this));
            row.addEventListener('mouseleave', this.onRowLeave.bind(this));
        });

        // 스크롤 애니메이션
        window.addEventListener('scroll', this.onScroll.bind(this));
    }

    /**
     * 초기 애니메이션 설정
     */
    initAnimations() {
        // CSS에서 초기 상태 설정
        const rankCards = document.querySelectorAll('.rank-card');
        rankCards.forEach(card => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
        });

        const tableRows = document.querySelectorAll('.ranking-table tbody tr');
        tableRows.forEach(row => {
            row.style.opacity = '0';
            row.style.transform = 'translateX(-20px)';
        });
    }

    /**
     * 상위 3명 시상대 애니메이션
     */
    showTopRankingsAnimation() {
        const rankCards = document.querySelectorAll('.rank-card');

        rankCards.forEach((card, index) => {
            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';

                // 1위는 추가 효과
                if (card.classList.contains('rank-1')) {
                    setTimeout(() => {
                        card.style.transform = 'translateY(0) scale(1.05)';
                        setTimeout(() => {
                            card.style.transform = 'translateY(0) scale(1)';
                        }, 200);
                    }, 300);
                }
            }, index * 200);
        });
    }

    /**
     * 테이블 행 순차 애니메이션
     */
    showTableRowsAnimation() {
        const tableRows = document.querySelectorAll('.ranking-table tbody tr');

        tableRows.forEach((row, index) => {
            setTimeout(() => {
                row.style.opacity = '1';
                row.style.transform = 'translateX(0)';
            }, 600 + (index * 50)); // 시상대 애니메이션 후에 시작
        });
    }

    /**
     * 내 랭킹 강조 효과
     */
    highlightMyRank() {
        const myRankRow = document.querySelector('.my-rank');
        if (myRankRow) {
            setTimeout(() => {
                myRankRow.style.animation = 'highlightPulse 2s ease-in-out';
            }, 1500);
        }
    }

    /**
     * 테이블 행 호버 이벤트
     */
    onRowHover(event) {
        const row = event.currentTarget;
        if (!row.classList.contains('my-rank')) {
            row.style.transform = 'translateX(5px)';
            row.style.transition = 'transform 0.2s ease';
        }
    }

    /**
     * 테이블 행 호버 해제 이벤트
     */
    onRowLeave(event) {
        const row = event.currentTarget;
        if (!row.classList.contains('my-rank')) {
            row.style.transform = 'translateX(0)';
        }
    }

    /**
     * 스크롤 이벤트 (추후 확장용)
     */
    onScroll() {
        // 추후 무한 스크롤이나 추가 애니메이션 구현시 사용
    }

    /**
     * 랭킹 데이터 새로고침 (API 연동용)
     */
    async refreshRankings() {
        try {
            const response = await fetch('/api/rankings');
            if (!response.ok) {
                throw new Error('Failed to fetch rankings');
            }

            const data = await response.json();
            this.updateRankingDisplay(data);

        } catch (error) {
            console.error('Error refreshing rankings:', error);
            this.showErrorMessage('랭킹 데이터를 불러오는데 실패했습니다.');
        }
    }

    /**
     * 랭킹 화면 업데이트
     */
    updateRankingDisplay(data) {
        // 추후 실시간 랭킹 업데이트 구현시 사용
        console.log('Updating ranking display:', data);
    }

    /**
     * 에러 메시지 표시
     */
    showErrorMessage(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;

        const container = document.querySelector('.container');
        container.insertBefore(errorDiv, container.firstChild);

        // 5초 후 자동 제거
        setTimeout(() => {
            errorDiv.remove();
        }, 5000);
    }

    /**
     * 성공 메시지 표시
     */
    showSuccessMessage(message) {
        const successDiv = document.createElement('div');
        successDiv.className = 'success-message';
        successDiv.style.cssText = `
            background: #e8f5e8;
            color: #2e7d32;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
            border-left: 4px solid #2e7d32;
        `;
        successDiv.textContent = message;

        const container = document.querySelector('.container');
        container.insertBefore(successDiv, container.firstChild);

        setTimeout(() => {
            successDiv.remove();
        }, 3000);
    }
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes highlightPulse {
        0%, 100% { transform: scale(1); }
        50% { transform: scale(1.02); }
    }

    .rank-card {
        transition: all 0.6s ease;
    }

    .ranking-table tbody tr {
        transition: all 0.4s ease;
    }
`;
document.head.appendChild(style);

// 페이지 로드시 인스턴스 생성
const rankingPage = new RankingPage();