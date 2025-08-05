document.addEventListener('DOMContentLoaded', function () {
    const ctx = document.getElementById('scoreChart');
    if (ctx) {
        const labels = JSON.parse(ctx.dataset.labels || '[]');
        const scores = JSON.parse(ctx.dataset.scores || '[]');

        new Chart(ctx, {
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
                    y: { beginAtZero: true, max: 100 }
                }
            }
        });
    }
});
