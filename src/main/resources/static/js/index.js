/* index.js - 메인 대시보드 차트 시각화 및 실시간 데이터 처리 */

document.addEventListener("DOMContentLoaded", function() {

    // 1. 초기 데이터 로드 및 차트 렌더링
    // [기술 포인트] HTML의 script 태그에서 선언한 전역 변수(window.dashboardData)를 가져와
    // 차트를 그립니다. 이는 SSR과 CSR의 매끄러운 연결을 보여줍니다.
    if (window.dashboardData && window.dashboardData.stats) {
        initDashboardCharts(window.dashboardData.stats);
    }

    // 2. 실시간 모니터링 시작 (Data Polling)
    startDataPolling();
});


// ---------------------------------------------------------
// 기능 1: 차트 초기화 (Visualization)
// ---------------------------------------------------------
function initDashboardCharts(data) {
    // 1-1. 장비 분류 차트 (Type Chart) - Doughnut Chart 사용
    const canvasType = document.getElementById('typeChart');
    if (canvasType) {
        const ctxType = canvasType.getContext('2d');

        // [디자인 포인트] 단순 단색이 아닌 그라데이션 컬러를 적용하여 입체감 부여
        const gradSvr = createGradient(ctxType, '#4ade80', '#15803d'); // Green
        const gradNet = createGradient(ctxType, '#facc15', '#a16207'); // Yellow
        const gradSto = createGradient(ctxType, '#22d3ee', '#0e7490'); // Cyan
        const gradUps = createGradient(ctxType, '#c084fc', '#7e22ce'); // Purple

        new Chart(ctxType, {
            type: 'doughnut',
            data: {
                labels: ['Server', 'Network', 'Storage', 'UPS'],
                datasets: [{
                    data: [data.svr, data.net, data.sto, data.ups],
                    backgroundColor: [gradSvr, gradNet, gradSto, gradUps],
                    borderWidth: 0,
                    hoverOffset: 15, // 마우스 오버 시 강조 효과
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
        const gradOn = createGradient(ctxStatus, '#34d399', '#059669'); // Success Color
        const gradOff = createGradient(ctxStatus, '#f87171', '#b91c1c'); // Danger Color

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
// 기능 2: 실시간 데이터 폴링 (Real-time Updates)
// ---------------------------------------------------------
// [기술 포인트] WebSocket 대신 Polling 방식을 사용하여
// 주기적으로 서버 상태를 체크합니다. (구현 난이도 대비 효율적)
function startDataPolling() {
    setInterval(() => {
        // 비동기 통신 (AJAX) - fetch API 사용
        fetch('/admin/api/env/now')
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(data => {
                // DOM 조작을 통한 화면 부분 갱신 (페이지 깜빡임 없음)

                // 1. PUE 값 업데이트
                const pueElem = document.getElementById('pueValue');
                if(pueElem) pueElem.innerText = data.currentPue.toFixed(2);

                // 2. 온도 업데이트
                const tempElem = document.getElementById('tempValue');
                if(tempElem) tempElem.innerText = data.currentTemp.toFixed(1);

                // 3. 팬 속도 업데이트
                const fanElem = document.getElementById('fanSpeedValue');
                if(fanElem) fanElem.innerText = data.fanSpeed;
            })
            .catch(error => console.error('Error fetching environment data:', error));
    }, 4000); // 4초마다 갱신
}


// ---------------------------------------------------------
// 유틸리티 함수들 (Helpers)
// ---------------------------------------------------------

// Canvas Gradient 생성 함수 (코드 재사용성)
function createGradient(ctx, colorStart, colorEnd) {
    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, colorStart);
    gradient.addColorStop(1, colorEnd);
    return gradient;
}

// 차트 공통 옵션 설정 (일관된 디자인 적용)
function getCommonChartOptions() {
    return {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    usePointStyle: true, // 포인트 스타일 범례
                    padding: 20,
                    font: { family: "'Noto Sans KR', sans-serif", weight: 'bold' },
                    color: '#94a3b8'
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
        cutout: '65%', // 도넛 차트 가운데 구멍 크기
        animation: {
            animateScale: true,
            animateRotate: true
        }
    };
}

// [UX] 헤더 스크롤 시 배경 변경 효과
window.addEventListener('scroll', function() {
    const navbar = document.querySelector('.navbar-custom');
    if (!navbar) return;
    if (window.scrollY > 50) {
        navbar.style.padding = '0.8rem 0';
    } else {
        navbar.style.padding = '1rem 0';
    }
});