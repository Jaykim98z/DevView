document.addEventListener("DOMContentLoaded", function () {
    /** =========================     *  1. 점수 변화 차트 (Line)     *  ========================= */
    const scoreCtx = document.getElementById("scoreChart");
    if (scoreCtx) {
        const labels = JSON.parse(scoreCtx.dataset.labels || '[]');
        const scores = JSON.parse(scoreCtx.dataset.scores || '[]');

        new Chart(scoreCtx, {
            type: 'line',
            data: {
                labels: labels.length > 0 ? labels : ["데이터 없음"],
                datasets: [{
                    label: '면접 점수',
                    data: scores.length > 0 ? scores : [0],
                    borderColor: '#4AB2E3',
                    backgroundColor: 'rgba(74, 178, 227, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.3,
                    pointBackgroundColor: '#4AB2E3',
                    pointRadius: 4
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false }, title: { display: false } },
                scales: { y: { beginAtZero: true, max: 100, ticks: { stepSize: 20 } } }
            }
        });
    }

    /** =========================     *  2. 관심 직무 차트 (Doughnut)     *  ========================= */
    const careerCtx = document.getElementById("careerChart");
    if (careerCtx) {
        const labels = JSON.parse(careerCtx.dataset.labels || '[]');
        const data = JSON.parse(careerCtx.dataset.data || '[]');

        new Chart(careerCtx, {
            type: 'doughnut',
            data: {
                labels: labels.length > 0 ? labels : ["데이터 없음"],
                datasets: [{
                    label: '관심 직무',
                    data: data.length > 0 ? data : [1],
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.6)',
                        'rgba(54, 162, 235, 0.6)',
                        'rgba(255, 206, 86, 0.6)',
                        'rgba(75, 192, 192, 0.6)',
                        'rgba(153, 102, 255, 0.6)',
                        'rgba(255, 159, 64, 0.6)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { position: 'bottom' }, title: { display: false } }
            }
        });
    }
});
