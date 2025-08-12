document.addEventListener('DOMContentLoaded', async function() {
    const scoreEl = document.getElementById('total-score');
    const gradeEl = document.getElementById('grade');
    const feedbackEl = document.getElementById('feedback-text');
    const summaryEl = document.getElementById('summary-text');
    const recommendationsEl = document.getElementById('recommendations-text');
    const skillTechScore = document.getElementById('skill-tech-score');
    const skillTechProgress = document.getElementById('skill-tech-progress');
    const skillProblemScore = document.getElementById('skill-problem-score');
    const skillProblemProgress = document.getElementById('skill-problem-progress');
    const skillCommScore = document.getElementById('skill-comm-score');
    const skillCommProgress = document.getElementById('skill-comm-progress');
    const skillAttitudeScore = document.getElementById('skill-attitude-score');
    const skillAttitudeProgress = document.getElementById('skill-attitude-progress');

    function setProgress(element, scoreElement, percentage) {
        scoreElement.textContent = `${percentage}%`;

        setTimeout(() => {
            element.style.width = `${percentage}%`;
        }, 100);
    }

    const pathParts = window.location.pathname.split('/');
    const interviewId = pathParts[pathParts.length - 1];

    if (!interviewId) {
        alert('Interview ID not found.');
        return;
    }

    try {
        const response = await fetch(`/api/v1/interviews/${interviewId}/results`);
        if (!response.ok) {
            throw new Error('Failed to fetch interview results.');
        }
        const result = await response.json();

        scoreEl.textContent = result.totalScore;
        gradeEl.textContent = `Grade ${result.grade}`;
        feedbackEl.textContent = result.feedback;
        recommendationsEl.textContent = result.recommendedResource;

        // TODO: Replace with real data when AI provides detailed scores
        // For now, we simulate the detailed scores based on the total score
        const techScore = result.totalScore * 0.95; // Simulate
        const problemScore = result.totalScore * 0.88; // Simulate
        const commScore = result.totalScore * 0.92; // Simulate
        const attitudeScore = result.totalScore * 0.85; // Simulate

        summaryEl.textContent = `전반적으로 우수한 역량을 보여주셨습니다. 특히 의사소통 능력과 기술 지식 면에서 뛰어난 성과를 거두었습니다. (This is a simulated summary)`;

        setProgress(skillTechProgress, skillTechScore, Math.round(techScore));
        setProgress(skillProblemProgress, skillProblemScore, Math.round(problemScore));
        setProgress(skillCommProgress, skillCommScore, Math.round(commScore));
        setProgress(skillAttitudeProgress, skillAttitudeScore, Math.round(attitudeScore));

    } catch (error) {
        console.error('Error fetching results:', error);
        feedbackEl.textContent = 'Failed to load results. Please try again later.';
    }
});
