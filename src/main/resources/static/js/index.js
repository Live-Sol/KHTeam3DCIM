/* index.js - 메인 대시보드 차트 및 UI 동작 제어 */

function initDashboardCharts(data) {

    // ---------------------------------------------------------
    // 1. 자산 분류 차트 (Type Chart) - 3D 입체 그라데이션 적용
    // 색상: SVR(초록), NET(노랑), STO(하늘), UPS(보라)
    // ---------------------------------------------------------
    const canvasType = document.getElementById('typeChart');
    if (canvasType) {
        const ctxType = canvasType.getContext('2d');

        // 그라데이션 생성 함수 (빛 반사 효과)
        function createGradient(ctx, colorStart, colorEnd) {
            const gradient = ctx.createLinearGradient(0, 0, 0, 400); // 위에서 아래로
            gradient.addColorStop(0, colorStart); // 밝은 색 (위쪽)
            gradient.addColorStop(1, colorEnd);   // 어두운 색 (아래쪽/그림자)
            return gradient;
        }

        // 각 항목별 3D 그라데이션 컬러 정의
        const gradSvr = createGradient(ctxType, '#4ade80', '#15803d'); // Green (밝은 초록 -> 짙은 초록)
        const gradNet = createGradient(ctxType, '#facc15', '#a16207'); // Yellow (밝은 노랑 -> 짙은 골드)
        const gradSto = createGradient(ctxType, '#22d3ee', '#0e7490'); // Sky Blue (형광 하늘 -> 짙은 청록)
        const gradUps = createGradient(ctxType, '#c084fc', '#7e22ce'); // Purple (밝은 보라 -> 짙은 보라)

        new Chart(ctxType, {
            type: 'doughnut',
            data: {
                labels: ['Server', 'Network', 'Storage', 'UPS'],
                datasets: [{
                    data: [data.svr, data.net, data.sto, data.ups],
                    backgroundColor: [
                        gradSvr, // 초록
                        gradNet, // 노랑
                        gradSto, // 하늘
                        gradUps  // 보라
                    ],
                    borderWidth: 0,
                    hoverOffset: 15, // 호버 시 더 많이 튀어나오게 (입체감)
                    hoverBorderWidth: 2,
                    hoverBorderColor: '#ffffff'
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
                            font: { family: "'Noto Sans KR', sans-serif", weight: 'bold' },
                            color: '#475569'
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(15, 23, 42, 0.95)',
                        padding: 12,
                        cornerRadius: 8,
                        titleFont: { size: 14, family: "'Orbitron', sans-serif" },
                        bodyFont: { size: 13 }
                    }
                },
                cutout: '65%', // 도넛 두께
                animation: {
                    animateScale: true,
                    animateRotate: true
                }
            }
        });
    }

    // ---------------------------------------------------------
    // 2. 가동 현황 차트 (Status Chart) - 3D 입체 그라데이션 적용
    // 색상: ON(에메랄드/초록 계열), OFF(레드 계열)
    // ---------------------------------------------------------
    const canvasStatus = document.getElementById('statusChart');
    if (canvasStatus) {
        const ctxStatus = canvasStatus.getContext('2d');

        // 그라데이션 생성
        function createGradient(ctx, colorStart, colorEnd) {
            const gradient = ctx.createLinearGradient(0, 0, 0, 400);
            gradient.addColorStop(0, colorStart);
            gradient.addColorStop(1, colorEnd);
            return gradient;
        }

        const gradOn = createGradient(ctxStatus, '#34d399', '#059669');  // Emerald (가동)
        const gradOff = createGradient(ctxStatus, '#f87171', '#b91c1c'); // Red (중지)

        new Chart(ctxStatus, {
            type: 'doughnut',
            data: {
                labels: ['Running (ON)', 'Stopped (OFF)'],
                datasets: [{
                    data: [data.on, data.off],
                    backgroundColor: [
                        gradOn,  // Live
                        gradOff  // Off
                    ],
                    borderWidth: 0,
                    hoverOffset: 15,
                    hoverBorderWidth: 2,
                    hoverBorderColor: '#ffffff'
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
                            font: { family: "'Noto Sans KR', sans-serif", weight: 'bold' },
                            color: '#475569'
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(15, 23, 42, 0.95)',
                        padding: 12,
                        cornerRadius: 8
                    }
                },
                cutout: '65%',
                animation: {
                    animateScale: true,
                    animateRotate: true
                }
            }
        });
    }
}

// 3. 헤더 스크롤 효과
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar-custom');
    if (!navbar) return;

    if (window.scrollY > 50) {
//        navbar.style.background = 'rgba(15, 23, 42, 0.95)';
        navbar.style.padding = '0.8rem 0';
    } else {
//        navbar.style.background = 'rgba(15, 23, 42, 0.85)';
        navbar.style.padding = '1rem 0';
    }
});