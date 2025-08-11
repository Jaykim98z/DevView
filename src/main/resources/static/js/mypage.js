document.addEventListener("DOMContentLoaded", function () {
    // 🔹 1. 점수 변화 차트 (Line)
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
                    borderColor: 'rgb(75, 192, 192)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.2
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100
                    }
                }
            }
        });
    }

    // 🔹 2. 관심 직무 차트 (Doughnut)캔버스에 그려져서 js에서 지정필요
    const careerCtx = document.getElementById("careerChart");
    if (careerCtx) {
        const labels = JSON.parse(careerCtx.dataset.labels || '[]');
        const data = JSON.parse(careerCtx.dataset.data || '[]');

        new Chart(careerCtx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    label: '관심 직무',
                    data: data,
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
                plugins: {
                    legend: { position: 'bottom' },
                    title: {
                        display: true,
                        text: '나의 관심 직무 통계'
                    }
                }
            }
        });
    }
});
