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
    // [A] 입고 희망일 최소 날짜 설정
    const today = new Date().toISOString().split('T')[0];
    const startDateInput = document.getElementById("startDate");
    if(startDateInput) startDateInput.setAttribute('min', today);

    // [B] 서버에서 넘어온 데이터에 따라 '기타'창 활성화 여부 결정
    const realUnitField = document.getElementById('realHeightUnit');
    const etcInput = document.getElementById('etcInput');
    const uEtcRadio = document.getElementById('u_etc');

    if (realUnitField && realUnitField.value) {
        const val = realUnitField.value;
        // 1, 2, 4가 아닌 값이 들어있다면 '기타'인 상황임
        if (!['1', '2', '4'].includes(val)) {
            if(etcInput) etcInput.disabled = false;
            if(uEtcRadio) uEtcRadio.checked = true;
        }
    }
}