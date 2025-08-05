document.addEventListener("DOMContentLoaded", function () {
    // 1. 면접 점수 line chart
    const scoreCtx = document.getElementById("scoreChart");
    if (scoreCtx) {
        const labels = JSON.parse(scoreCtx.dataset.labels || '[]');
        const scores = JSON.parse(scoreCtx.dataset.scores || '[]');

        new Chart(scoreCtx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: '면접 점수',
                    data: scores,
                    borderWidth: 2,
                    fill: false,
                    tension: 0.2
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100
                    }
                }
            }
        });
    }

    // 2. 관심 직무 doughnut chart
    const careerCtx = document.getElementById("careerChart");
    if (careerCtx) {
        new Chart(careerCtx, {
            type: 'doughnut',
            data: {
                labels: ['Frontend', 'Backend', 'AI', 'Data', 'DevOps'],
                datasets: [{
                    label: '직무 분포',
                    data: [25, 30, 20, 15, 10],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    title: {
                        display: true,
                        text: '나의 관심 직무 통계'
                    }
                }
            }
        });
    }
});
