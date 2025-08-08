document.addEventListener('DOMContentLoaded', function() {
    const interviewForm = document.getElementById('interview-form');

    if (interviewForm) {
        interviewForm.addEventListener('submit', async function(event) {
            event.preventDefault();

            // Show a loading indicator if you have one
            // document.getElementById('loadingSpinner').style.display = 'block';

            const startRequest = {
                userId: 1, // TODO: Replace with actual logged-in user ID
                interviewType: "PRACTICE", // TODO: Get from user selection
                jobPosition: "Backend Developer", // TODO: Get from user selection
                careerLevel: "JUNIOR" // TODO: Get from user selection
            };

            try {
                // Start Interview API Call
                const startResponse = await fetch('/api/v1/interviews/start', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(startRequest)
                });

                if (!startResponse.ok) throw new Error('Failed to start interview.');
                const interviewData = await startResponse.json();
                const interviewId = interviewData.interviewId;

                // Fetch Questions API Call
                const questionsResponse = await fetch(`/api/v1/interviews/${interviewId}/questions`, {
                    method: 'POST'
                });

                if (!questionsResponse.ok) throw new Error('Failed to fetch questions.');
                const questions = await questionsResponse.json();

                // Save data to localStorage to pass to the next page
                localStorage.setItem('interviewId', interviewId);
                localStorage.setItem('questions', JSON.stringify(questions));

                // Redirect to the interview session page
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
