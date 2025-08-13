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
        const analysis = JSON.parse(result.feedback);

        scoreEl.textContent = analysis.totalScore;
        gradeEl.textContent = `Grade ${result.grade}`;
        feedbackEl.textContent = analysis.feedback;
        summaryEl.textContent = analysis.summary;
        recommendationsEl.innerHTML = result.recommendedResource;

        setProgress(skillTechProgress, skillTechScore, analysis.techScore);
        setProgress(skillProblemProgress, skillProblemScore, analysis.problemScore);
        setProgress(skillCommProgress, skillCommScore, analysis.commScore);
        setProgress(skillAttitudeProgress, skillAttitudeScore, analysis.attitudeScore);

    } catch (error) {
        console.error('Error fetching results:', error);
        feedbackEl.textContent = 'Failed to load results. Please try again later.';
    }
});
