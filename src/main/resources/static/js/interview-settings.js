document.addEventListener('DOMContentLoaded', function() {
    const interviewForm = document.getElementById('interview-form');
    const optionGroups = document.querySelectorAll('.options-group');

    // CSRF 토큰 초기화
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    optionGroups.forEach(group => {
        if (group.id !== 'question-count-group') {
            group.addEventListener('click', function(event) {
                const clickedButton = event.target.closest('.option-btn');
                if (clickedButton) {
                    const buttonsInGroup = group.querySelectorAll('.option-btn');
                    buttonsInGroup.forEach(btn => btn.classList.remove('active'));
                    clickedButton.classList.add('active');
                }
            });
        }
    });

    if (interviewForm) {
        interviewForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            spinner.show();

            const currentUserId = document.getElementById('current-user-id').value;
            if (!currentUserId) {
                alert("Could not find user ID. Please make sure you are logged in.");
                return;
            }

            const careerLevel = document.querySelector('#career-group .active').dataset.value;
            const jobPosition = document.querySelector('#position-group .active').dataset.value;
            const interviewType = document.querySelector('#type-group .active').dataset.value;
            const questionCount = document.getElementById('question-count-select').value;
            const durationMinutes = document.getElementById('duration-select').value;

            const startRequest = {
                userId: currentUserId,
                interviewType: interviewType,
                jobPosition: jobPosition,
                careerLevel: careerLevel,
                questionCount: parseInt(questionCount, 10),
                durationMinutes: parseInt(durationMinutes, 10)
            };

            try {
                // CSRF 토큰 포함한 headers
                const headers = { 'Content-Type': 'application/json' };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                const startResponse = await fetch('/api/v1/interviews/start', {
                    method: 'POST',
                    headers: headers,
                    credentials: 'same-origin',
                    body: JSON.stringify(startRequest)
                });
                if (!startResponse.ok) throw new Error('Failed to start interview.');

                const interviewData = await startResponse.json();
                const interviewId = interviewData.interviewId;

                const questionsResponse = await fetch(`/api/v1/interviews/${interviewId}/questions`, {
                    method: 'POST',
                    headers: headers,
                    credentials: 'same-origin'
                });
                if (!questionsResponse.ok) throw new Error('Failed to fetch questions.');

                const questions = await questionsResponse.json();

                localStorage.setItem('interviewId', interviewId);
                localStorage.setItem('questions', JSON.stringify(questions));
                localStorage.setItem('durationMinutes', startRequest.durationMinutes);

                window.location.href = '/interview/session';

            } catch (error) {
                console.error('Error:', error);
                alert('An error occurred. Please try again.');
                spinner.hide();
            }
        });
    }
});