/* index.js - 메인 대시보드 차트 및 동작 제어 */

// 차트 초기화 함수 (HTML에서 데이터 받아옴)
function initDashboardCharts(data) {
    
    // 1. 자산 분포 차트 (Type Chart)
    const ctxType = document.getElementById('typeChart');
    if (ctxType) {
        new Chart(ctxType, {
            type: 'doughnut',
            data: {
                labels: ['Server', 'Network', 'Storage', 'UPS'],
                datasets: [{
                    data: [data.svr, data.net, data.sto, data.ups],
                    backgroundColor: ['#198754', '#ffc107', '#0dcaf0', '#6f42c1'],
                    borderWidth: 2,
                    borderColor: '#ffffff',
                    hoverOffset: 10
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20,
                            font: { family: "'Noto Sans KR', sans-serif" }
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0,0,0,0.8)',
                        padding: 10,
                        cornerRadius: 8
                    }
                },
                cutout: '65%' // 도넛 두께 조절
            }
        });
    }

    // 2. 가동 현황 차트 (Status Chart)
    const ctxStatus = document.getElementById('statusChart');
    if (ctxStatus) {
        new Chart(ctxStatus, {
            type: 'pie',
            data: {
                labels: ['Running (ON)', 'Stopped (OFF)'],
                datasets: [{
                    data: [data.on, data.off],
                    backgroundColor: ['#198754', '#dc3545'],
                    borderWidth: 2,
                    borderColor: '#ffffff',
                    hoverOffset: 10
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20,
                            font: { family: "'Noto Sans KR', sans-serif" }
                        }
                    }
                }
            }
        });
    }
}