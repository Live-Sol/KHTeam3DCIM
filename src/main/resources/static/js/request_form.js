// 1. 표준 유닛(1, 2, 4U) 선택 시
function selectUnit(val) {
    document.getElementById('realHeightUnit').value = val;
    document.getElementById('etcInput').value = '';
    document.getElementById('etcInput').disabled = true;
}

// 2. '기타' 라디오 버튼 클릭 시
function enableEtc() {
    document.getElementById('etcInput').disabled = false;
    document.getElementById('etcInput').focus();
    // 기존에 써둔 값이 있다면 유지, 없다면 비움
    document.getElementById('realHeightUnit').value = document.getElementById('etcInput').value;
}

// 3. 기타 입력창에 숫자 타이핑 시
function updateEtcValue() {
    document.getElementById('realHeightUnit').value = document.getElementById('etcInput').value;
}

// 4. 페이지 로드 시 복원 로직
window.onload = function() {
    // [A] 기존 입고 희망일 최소 날짜 설정 로직...
    const today = new Date().toISOString().split('T')[0];
    const startDateInput = document.getElementById("startDate");
    if(startDateInput) startDateInput.setAttribute('min', today);

    // [B] 기존 '기타'창 활성화 여부 결정 로직...
    const realUnitField = document.getElementById('realHeightUnit');
    const etcInput = document.getElementById('etcInput');
    const uEtcRadio = document.getElementById('u_etc');
    if (realUnitField && realUnitField.value) {
        const val = realUnitField.value;
        if (!['1', '2', '4'].includes(val)) {
            if(etcInput) etcInput.disabled = false;
            if(uEtcRadio) uEtcRadio.checked = true;
        }
    }

    // [C] 추가: 토스트 알림 실행 로직
    // hasErrors가 true일 때 Bootstrap 토스트를 띄웁니다.
    if (typeof hasErrors !== 'undefined' && hasErrors) {
        const toastElement = document.getElementById('errorToast');
        if (toastElement) {
            // Bootstrap 5의 Toast 인스턴스 생성 및 출력
            const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
            toast.show();
        }
    }
}