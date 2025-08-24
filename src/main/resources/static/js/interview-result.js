document.addEventListener('DOMContentLoaded', async function() {
    const scoreEl = document.getElementById('total-score');
    const gradeEl = document.getElementById('grade');
    const feedbackContainer = document.getElementById('feedback-text');
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

    spinner.show();

    try {
        const response = await fetch(`/api/v1/interviews/${interviewId}/results`, {
            credentials: 'same-origin'  // CSRF: credentials 추가
        });

        if (!response.ok) {
            throw new Error('Failed to fetch interview results.');
        }
        const result = await response.json();
        const analysis = JSON.parse(result.feedback);

        scoreEl.textContent = analysis.totalScore;
        gradeEl.textContent = `Grade ${result.grade}`;
        summaryEl.textContent = analysis.summary;

        feedbackContainer.innerHTML = '';
        if (analysis.detailedFeedback && analysis.detailedFeedback.length > 0) {
            analysis.detailedFeedback.forEach(item => {
                const feedbackItem = document.createElement('div');
                feedbackItem.className = 'feedback-item';

                const questionHeader = document.createElement('div');
                questionHeader.className = 'feedback-question';
                questionHeader.textContent = `Q: ${item.question}`;

                const content = document.createElement('div');
                content.className = 'feedback-content';

                const answerElement = document.createElement('p');
                answerElement.textContent = `A: ${item.answer}`;

                const feedbackElement = document.createElement('p');
                feedbackElement.textContent = `Feedback: ${item.feedback}`;

                content.appendChild(answerElement);
                content.appendChild(feedbackElement);

                feedbackItem.appendChild(questionHeader);
                feedbackItem.appendChild(content);
                feedbackContainer.appendChild(feedbackItem);

                questionHeader.addEventListener('click', () => {
                    questionHeader.classList.toggle('active');
                    if (content.style.maxHeight) {
                        content.style.maxHeight = null;
                        content.style.padding = '0 1.5rem';
                    } else {
                        content.style.maxHeight = content.scrollHeight + "px";
                        content.style.padding = '1rem 1.5rem';
                    }
                });
            });
        } else {
            feedbackContainer.textContent = "No detailed feedback available.";
        }
        recommendationsEl.innerHTML = result.recommendedResource;
        if (result.recommendedResource && result.recommendedResource.includes("생성하는 중")) {
            startPollingForRecommendations(interviewId);
        }

        setProgress(skillTechProgress, skillTechScore, analysis.techScore);
        setProgress(skillProblemProgress, skillProblemScore, analysis.problemScore);
        setProgress(skillCommProgress, skillCommScore, analysis.commScore);
        setProgress(skillAttitudeProgress, skillAttitudeScore, analysis.attitudeScore);

    } catch (error) {
        console.error('Error fetching results:', error);
        feedbackContainer.textContent = 'Failed to load results. Please try again later.';
    } finally {
        spinner.hide();
    }

    function startPollingForRecommendations(interviewId) {
        let attempts = 0;
        const maxAttempts = 12;
        const pollInterval = 10000;

        const intervalId = setInterval(async () => {
            if (attempts >= maxAttempts) {
                clearInterval(intervalId);
                document.getElementById('recommendations-text').textContent = "추천 자료 생성에 실패했습니다. 나중에 다시 시도해주세요.";
                return;
            }

            try {
                const response = await fetch(`/api/v1/interviews/${interviewId}/results`, {
                    credentials: 'same-origin'  // CSRF: credentials 추가
                });
                if (!response.ok) {
                    throw new Error('Failed to fetch interview results.');
                }
                const result = await response.json();

                if (result.recommendedResource && !result.recommendedResource.includes("생성하는 중")) {
                    document.getElementById('recommendations-text').innerHTML = result.recommendedResource;
                    clearInterval(intervalId);
                }
            } catch (error) {
                console.error("Polling failed:", error);
                clearInterval(intervalId);
            }

            attempts++;
        }, pollInterval);
    }
});