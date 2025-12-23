/* index.js - 메인 대시보드 차트 및 UI 동작 제어 통합 */

document.addEventListener("DOMContentLoaded", function() {

    // 1. Thymeleaf에서 전달받은 데이터로 차트 초기화
    if (window.dashboardData && window.dashboardData.stats) {
        initDashboardCharts(window.dashboardData.stats);
    }

    // 2. 실시간 데이터 폴링 (서버 데이터 갱신 - 2초 주기)
    startDataPolling();
});


// ---------------------------------------------------------
// 기능 1: 차트 초기화 함수
// ---------------------------------------------------------
function initDashboardCharts(data) {
    // 1-1. 자산 분류 차트 (Type Chart)
    const canvasType = document.getElementById('typeChart');
    if (canvasType) {
        const ctxType = canvasType.getContext('2d');
        const gradSvr = createGradient(ctxType, '#4ade80', '#15803d');
        const gradNet = createGradient(ctxType, '#facc15', '#a16207');
        const gradSto = createGradient(ctxType, '#22d3ee', '#0e7490');
        const gradUps = createGradient(ctxType, '#c084fc', '#7e22ce');

        new Chart(ctxType, {
            type: 'doughnut',
            data: {
                labels: ['Server', 'Network', 'Storage', 'UPS'],
                datasets: [{
                    data: [data.svr, data.net, data.sto, data.ups],
                    backgroundColor: [gradSvr, gradNet, gradSto, gradUps],
                    borderWidth: 0,
                    hoverOffset: 15,
                    hoverBorderWidth: 2,
                    hoverBorderColor: '#ffffff'
                }]
            },
            options: getCommonChartOptions()
        });
    }

    // 1-2. 가동 현황 차트 (Status Chart)
    const canvasStatus = document.getElementById('statusChart');
    if (canvasStatus) {
        const ctxStatus = canvasStatus.getContext('2d');
        const gradOn = createGradient(ctxStatus, '#34d399', '#059669');
        const gradOff = createGradient(ctxStatus, '#f87171', '#b91c1c');

        new Chart(ctxStatus, {
            type: 'doughnut',
            data: {
                labels: ['Running (ON)', 'Stopped (OFF)'],
                datasets: [{
                    data: [data.on, data.off],
                    backgroundColor: [gradOn, gradOff],
                    borderWidth: 0,
                    hoverOffset: 15,
                    hoverBorderWidth: 2,
                    hoverBorderColor: '#ffffff'
                }]
            },
            options: getCommonChartOptions()
        });
    }
}


// ---------------------------------------------------------
// 기능 2: 서버 데이터 폴링 (실제 데이터 갱신)
// ---------------------------------------------------------
function startDataPolling() {
    setInterval(() => {
        fetch('/admin/api/env/now')
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(data => {
                // PUE 업데이트 (서버의 기준값으로 리셋해줌, 시뮬레이션은 이 위에서 다시 뜀)
                const pueElem = document.getElementById('pueValue');
                if(pueElem) pueElem.innerText = data.currentPue.toFixed(2);

                // 온도 업데이트
                const tempElem = document.getElementById('tempValue');
                if(tempElem) tempElem.innerText = data.currentTemp.toFixed(1);

                // 팬 속도 업데이트
                const fanElem = document.getElementById('fanSpeedValue');
                if(fanElem) fanElem.innerText = data.fanSpeed;
            })
            .catch(error => console.error('Error fetching environment data:', error));
    }, 4000); // 2초 주기
}


// ---------------------------------------------------------
// 유틸리티 함수들
// ---------------------------------------------------------

// 그라데이션 생성 헬퍼
function createGradient(ctx, colorStart, colorEnd) {
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, colorStart);
    gradient.addColorStop(1, colorEnd);
    return gradient;
}

// 공통 차트 옵션
function getCommonChartOptions() {
    return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    usePointStyle: true,
                    padding: 20,
                    font: { family: "'Noto Sans KR', sans-serif", weight: 'bold' },
                    color: '#94a3b8' // 텍스트 색상 수정 (더 잘 보이게)
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
        cutout: '65%',
        animation: {
            animateScale: true,
            animateRotate: true
        }
    };
}

// 헤더 스크롤 효과
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar-custom');
    if (!navbar) return;
    if (window.scrollY > 50) {
        navbar.style.padding = '0.8rem 0';
    } else {
        navbar.style.padding = '1rem 0';
    }
});