document.addEventListener('DOMContentLoaded', async function() {
    // URL 경로에서 interviewId를 추출합니다.
    const pathParts = window.location.pathname.split('/');
    const interviewId = pathParts[pathParts.length - 1];

    if (!interviewId) {
        alert('Interview ID not found.');
        return;
    }

    // DOM 요소 가져오기
    const scoreEl = document.getElementById('total-score');
    const gradeEl = document.getElementById('grade');
    const feedbackEl = document.getElementById('feedback-text');

    try {
        // 면접 결과 조회 API 호출
        const response = await fetch(`/api/v1/interviews/${interviewId}/results`);

        if (!response.ok) {
            throw new Error('Failed to fetch interview results.');
        }

        const result = await response.json();

        // API 응답 데이터로 화면 업데이트
        scoreEl.textContent = result.totalScore;
        gradeEl.textContent = `Grade ${result.grade}`;
        feedbackEl.textContent = result.feedback;

    } catch (error) {
        console.error('Error fetching results:', error);
        feedbackEl.textContent = 'Failed to load results. Please try again later.';
    }
});
