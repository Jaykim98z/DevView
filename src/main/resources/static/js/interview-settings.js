document.addEventListener('DOMContentLoaded', function() {
    const interviewForm = document.getElementById('interview-form');
    const optionGroups = document.querySelectorAll('.options-group');

    optionGroups.forEach(group => {
            group.addEventListener('click', function(event) {
                if (event.target.classList.contains('option-btn')) {
                    const buttonsInGroup = group.querySelectorAll('.option-btn');
                    buttonsInGroup.forEach(btn => btn.classList.remove('active'));

                    event.target.classList.add('active');
                }
            });
        });

    if (interviewForm) {
        interviewForm.addEventListener('submit', async function(event) {
            event.preventDefault();

            const currentUserId = document.getElementById('current-user-id').value;
            const careerLevel = document.querySelector('button[data-value*="LEVEL"].active')?.dataset.value || 'JUNIOR';
            const jobPosition = document.querySelector('button[data-value*="END"].active, button[data-value*="STACK"].active')?.dataset.value || 'BACKEND';
            const interviewType = document.querySelector('#type-group .option-btn.active').dataset.value;

            const startRequest = {
                userId: currentUserId,
                interviewType: interviewType,
                jobPosition: jobPosition,
                careerLevel: careerLevel
            };

            try {
                const startResponse = await fetch('/api/v1/interviews/start', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(startRequest)
                });

                if (!startResponse.ok) throw new Error('Failed to start interview.');
                const interviewData = await startResponse.json();
                const interviewId = interviewData.interviewId;

                const questionsResponse = await fetch(`/api/v1/interviews/${interviewId}/questions`, {
                    method: 'POST'
                });

                if (!questionsResponse.ok) throw new Error('Failed to fetch questions.');
                const questions = await questionsResponse.json();

                localStorage.setItem('interviewId', interviewId);
                localStorage.setItem('questions', JSON.stringify(questions));

                window.location.href = '/interview/session';

            } catch (error) {
                console.error('Error:', error);
                alert('An error occurred. Please try again.');
                // Hide loading indicator on error
                // document.getElementById('loadingSpinner').style.display = 'none';
            }
        });
    }
});
