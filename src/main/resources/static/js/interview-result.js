document.addEventListener('DOMContentLoaded', async function() {
    const pathParts = window.location.pathname.split('/');
    const interviewId = pathParts[pathParts.length - 1];

    if (!interviewId) {
        alert('Interview ID not found.');
        return;
    }

    const scoreEl = document.getElementById('total-score');
    const gradeEl = document.getElementById('grade');
    const feedbackEl = document.getElementById('feedback-text');
    const recommendationsEl = document.getElementById('recommendations-text');

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

    } catch (error) {
        console.error('Error fetching results:', error);
        feedbackEl.textContent = 'Failed to load results. Please try again later.';
    }
});
