document.addEventListener('DOMContentLoaded', function() {
    // State variables
    let interviewId = null;
    let questions = [];
    let currentQuestionIndex = 0;
    let userAnswers = [];
    let timerInterval;

    // DOM Elements
    const questionCounterEl = document.getElementById('question-counter');
    const questionTextEl = document.getElementById('question-text');
    const answerTextareaEl = document.getElementById('answer-textarea');
    const submitAnswerBtnEl = document.getElementById('submit-answer-btn');
    const progressListEl = document.getElementById('progress-list');
    const timerEl = document.getElementById('timer');
    const charCounterEl = document.getElementById('char-counter');

    function initializeInterview() {
        interviewId = localStorage.getItem('interviewId');
        const storedQuestions = localStorage.getItem('questions');

        if (!interviewId || !storedQuestions) {
            alert('Interview data not found.');
            window.location.href = '/interview/settings';
            return;
        }

        questions = JSON.parse(storedQuestions);
        setupProgressTracker();
        displayCurrentQuestion();
        startTimer(15 * 60);
    }

    function displayCurrentQuestion() {
        const totalQuestions = questions.length;
        if (currentQuestionIndex >= totalQuestions) return;

        const currentQuestion = questions[currentQuestionIndex];
        questionCounterEl.textContent = `질문 ${currentQuestionIndex + 1}/${totalQuestions}`;
        questionTextEl.textContent = currentQuestion.text;
        answerTextareaEl.value = '';
        updateCharCounter();
        updateProgressTrackerUI();

        if (currentQuestionIndex === totalQuestions - 1) {
            submitAnswerBtnEl.textContent = '면접 완료하기';
        }
    }

    function setupProgressTracker() {
        progressListEl.innerHTML = '';
        questions.forEach((q, index) => {
            const li = document.createElement('li');
            li.id = `progress-item-${index}`;
            li.textContent = `질문 ${index + 1}`;
            progressListEl.appendChild(li);
        });
    }

    function updateProgressTrackerUI() {
        for (let i = 0; i < questions.length; i++) {
            const item = document.getElementById(`progress-item-${i}`);
            if (i < currentQuestionIndex) {
                item.className = 'completed';
            } else if (i === currentQuestionIndex) {
                item.className = 'current';
            } else {
                item.className = '';
            }
        }
    }

    function updateCharCounter() {
        const textLength = answerTextareaEl.value.length;
        charCounterEl.textContent = `${textLength}/500 글자`;
    }

    function startTimer(durationInSeconds) {
        let remainingTime = durationInSeconds;
        clearInterval(timerInterval);

        timerInterval = setInterval(() => {
            if (remainingTime < 0) {
                clearInterval(timerInterval);
                endInterview();
                return;
            }

            const minutes = Math.floor(remainingTime / 60);
            const seconds = remainingTime % 60;

            timerEl.textContent = `시간: ${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;

            remainingTime--;
        }, 1000);
    }

    async function endInterview() {
        submitAnswerBtnEl.textContent = 'Submitting...';
        spinner.show();
        submitAnswerBtnEl.disabled = true;
        clearInterval(timerInterval);

        const submitRequest = {
            interviewId: interviewId,
            answers: userAnswers
        };

        try {
            const submitResponse = await fetch('/api/v1/interviews/answers', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(submitRequest)
            });

            if (!submitResponse.ok) throw new Error('Failed to submit answers.');

            const endResponse = await fetch(`/api/v1/interviews/${interviewId}/end`, {
                method: 'POST'
            });

            if (!endResponse.ok) throw new Error('Failed to end interview.');

            localStorage.removeItem('interviewId');
            localStorage.removeItem('questions');

            window.location.href = `/interview/result/${interviewId}`;

        } catch (error) {
            console.error('Error ending interview:', error);
            alert('Failed to submit results. Please try again.');
            submitAnswerBtnEl.textContent = 'Submit Answer';
            submitAnswerBtnEl.disabled = false;
            spinner.hide();
        }
    }

    answerTextareaEl.addEventListener('input', updateCharCounter);

    submitAnswerBtnEl.addEventListener('click', async function() {
        userAnswers.push({
            questionId: questions[currentQuestionIndex].questionId,
            answerText: answerTextareaEl.value
        });

        currentQuestionIndex++;

        if (currentQuestionIndex < questions.length) {
            displayCurrentQuestion();
        } else {
            await endInterview();
        }
    });

    initializeInterview();
});
