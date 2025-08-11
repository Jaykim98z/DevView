document.addEventListener('DOMContentLoaded', function() {
    // State variables to manage the interview
    let interviewId = null;
    let questions = [];
    let currentQuestionIndex = 0;
    let userAnswers = [];
    let totalTime = 15 * 60;
    let intervalId;

    // DOM Elements
    const questionCounterEl = document.getElementById('question-counter');
    const questionTextEl = document.getElementById('question-text');
    const answerTextareaEl = document.getElementById('answer-textarea');
    const submitAnswerBtnEl = document.getElementById('submit-answer-btn');
    const progressListEl = document.getElementById('progress-list');
    const timerEl = document.getElementById('timer');
    const charCounterEl = document.getElementById('char-counter');

    function startTimer() {
        intervalId = setInterval(() => {
            const minutes = Math.floor(totalTime / 60);
            const seconds = totalTime % 60;

            timerEl.textContent = `시간: ${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;

            if (totalTime <= 0) {
                clearInterval(intervalId);
                // Optional: Auto-submit or end interview when time runs out
            }
            totalTime--;
        }, 1000);
    }

    answerTextareaEl.addEventListener('input', () => {
        const textLength = answerTextareaEl.value.length;
        charCounterEl.textContent = `${textLength}/500 글자`;
    });

    // --- Initialization ---
    function initializeInterview() {
        // Retrieve data from the previous page
        interviewId = localStorage.getItem('interviewId');
        const storedQuestions = localStorage.getItem('questions');

        if (!interviewId || !storedQuestions) {
            alert('Interview data not found. Returning to settings.');
            window.location.href = '/interview/settings';
            return;
        }

        questions = JSON.parse(storedQuestions);

        if (questions.length === 0) {
            alert('No questions found for this interview.');
            window.location.href = '/interview/settings';
            return;
        }

        // Setup the progress tracker
        setupProgressTracker();

        // Display the first question
        displayCurrentQuestion();

        // Start the main interview timer
        startTimer();
    }

    // --- UI Update Functions ---
    function displayCurrentQuestion() {
        const currentQuestion = questions[currentQuestionIndex];
        questionCounterEl.textContent = `질문 ${currentQuestionIndex + 1}/${questions.length}`;
        questionTextEl.textContent = currentQuestion.text;

        // Update progress tracker UI
        updateProgressTrackerUI();

        // Clear the textarea for the new question
        answerTextareaEl.value = '';
    }

    function setupProgressTracker() {
        questions.forEach((q, index) => {
            const li = document.createElement('li');
            li.id = `progress-item-${index}`;
            li.textContent = `질문 ${index + 1}`;
            progressListEl.appendChild(li);
        });
    }

    function updateProgressTrackerUI() {
        // Mark previous questions as completed
        for (let i = 0; i < currentQuestionIndex; i++) {
            document.getElementById(`progress-item-${i}`).className = 'completed';
        }
        // Mark the current question as active
        document.getElementById(`progress-item-${currentQuestionIndex}`).className = 'current';
    }

    // --- Start the interview ---
    initializeInterview();

    // --- Event Listener for the Submit Button ---
    submitAnswerBtnEl.addEventListener('click', async function() {
        // 1. Store the current answer
        const currentAnswer = {
            questionId: questions[currentQuestionIndex].questionId,
            answerText: answerTextareaEl.value
        };
        userAnswers.push(currentAnswer);

        // 2. Move to the next question
        currentQuestionIndex++;

        // 3. Check if the interview is over
        if (currentQuestionIndex < questions.length) {
            // If there are more questions, display the next one
            displayCurrentQuestion();
        } else {
            // If all questions are answered, end the interview
            await endInterview();
        }
    });

    // --- New function to handle ending the interview ---
    async function endInterview() {
        // Change button to show it's submitting
        submitAnswerBtnEl.textContent = 'Submitting...';
        submitAnswerBtnEl.disabled = true;

        const submitRequest = {
            interviewId: interviewId,
            answers: userAnswers
        };

        try {
            // Call the batch answer submission API
            const submitResponse = await fetch('/api/v1/interviews/answers', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(submitRequest)
            });

            if (!submitResponse.ok) throw new Error('Failed to submit answers.');

            // Call the end interview API to generate results
            const endResponse = await fetch(`/api/v1/interviews/${interviewId}/end`, {
                method: 'POST'
            });

            if (!endResponse.ok) throw new Error('Failed to end interview.');

            // Redirect to the result page
            window.location.href = `/interview/result/${interviewId}`; // We'll create this page next

        } catch (error) {
            console.error('Error ending interview:', error);
            alert('Failed to submit results. Please try again.');
            submitAnswerBtnEl.textContent = 'Submit Answer';
            submitAnswerBtnEl.disabled = false;
        }
    }
});
