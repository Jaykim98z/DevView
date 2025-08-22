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

    // CSRF 토큰 초기화
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    function initializeInterview() {
        interviewId = localStorage.getItem('interviewId');
        const storedQuestions = localStorage.getItem('questions');
        const durationMinutes = localStorage.getItem('durationMinutes');

        if (!interviewId || !storedQuestions) {
            alert('Interview data not found. Redirecting to settings.');
            window.location.href = '/interview/settings';
            return;
        }

        questions = JSON.parse(storedQuestions);
        setupProgressTracker();
        displayCurrentQuestion();
        startTimer(parseInt(durationMinutes, 10) * 60);
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

        if (textLength > 500) {
            charCounterEl.classList.add('error');
            submitAnswerBtnEl.disabled = true;
        } else {
            charCounterEl.classList.remove('error');
            submitAnswerBtnEl.disabled = false;
        }
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
            // CSRF 토큰 포함한 headers
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            const submitResponse = await fetch('/api/v1/interviews/answers', {
                method: 'POST',
                headers: headers,
                credentials: 'same-origin',
                body: JSON.stringify(submitRequest)
            });

            if (!submitResponse.ok) throw new Error('Failed to submit answers.');

            const endResponse = await fetch(`/api/v1/interviews/${interviewId}/end`, {
                method: 'POST',
                headers: headers,
                credentials: 'same-origin'
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
            if (confirm("답변을 제출하고 면접을 완료하시겠습니까?")) {
                await endInterview();
            }
        }
    });

    initializeInterview();
});