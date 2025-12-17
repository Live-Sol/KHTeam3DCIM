/* index.js - 메인 대시보드 차트 및 UI 동작 제어 */

function initDashboardCharts(data) {

    // 1. 자산 분포 차트 (Type Chart) - 디자인 개선
    const ctxType = document.getElementById('typeChart');
    if (ctxType) {
        new Chart(ctxType, {
            type: 'doughnut',
            data: {
                labels: ['Server', 'Network', 'Storage', 'UPS'],
                datasets: [{
                    data: [data.svr, data.net, data.sto, data.ups],
                    backgroundColor: [
                        '#00c6ff', // Cyan-Blue (SVR)
                        '#0072ff', // Deep Blue (NET)
                        '#8b5cf6', // Purple (STO)
                        '#a855f7'  // Light Purple (UPS)
                    ],
                    borderWidth: 0, // 테두리 없음 (깔끔하게)
                    hoverOffset: 4
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
                        backgroundColor: 'rgba(15, 23, 42, 0.9)', // 짙은 남색 배경
                        padding: 12,
                        cornerRadius: 8,
                        titleFont: { family: "'Noto Sans KR', sans-serif" }
                    }
                },
                cutout: '70%' // 도넛 구멍을 더 크게
            }
        });
    }

    // 2. 가동 현황 차트 (Status Chart) - 디자인 개선
    const ctxStatus = document.getElementById('statusChart');
    if (ctxStatus) {
        new Chart(ctxStatus, {
            type: 'doughnut', // 파이 차트보다 도넛이 더 현대적임
            data: {
                labels: ['Running (ON)', 'Stopped (OFF)'],
                datasets: [{
                    data: [data.on, data.off],
                    backgroundColor: [
                        '#10b981', // Emerald (ON)
                        '#ef4444'  // Red (OFF)
                    ],
                    borderWidth: 0,
                    hoverOffset: 4
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
                        backgroundColor: 'rgba(15, 23, 42, 0.9)',
                        padding: 12,
                        cornerRadius: 8
                    }
                },
                cutout: '70%'
            }
        });
    }
}

// 3. 헤더 스크롤 효과 (투명 -> 불투명)
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar-custom');
    if (!navbar) return;

    if (window.scrollY > 50) {
        // 스크롤 내리면 배경을 더 진하게
        navbar.style.background = 'rgba(15, 23, 42, 0.95)';
        navbar.style.padding = '0.8rem 0';
    } else {
        // 최상단이면 약간 투명하고 넓게
        navbar.style.background = 'rgba(15, 23, 42, 0.85)';
        navbar.style.padding = '1rem 0';
    }
});