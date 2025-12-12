/* device_form.js : 장비 등록/수정 화면 전용 스크립트 */

/**
 * 대기 중인 신청서(SelectBox) 선택 시,
 * 해당 신청서의 정보를 폼(Input)에 자동 입력하는 함수
 */
function loadRequestData(selectObj) {
    // 1. 선택된 옵션 태그 가져오기
    const selectedOption = selectObj.options[selectObj.selectedIndex];

    // 2. '선택 안 함'일 경우: 히든 필드 비우고 종료
    if (selectObj.value === "") {
        const reqField = document.getElementById('reqIdField');
        if(reqField) reqField.value = "";
        return;
    }

    // 3. data-* 속성에서 값 꺼내기
    // (HTML에서 th:data-vendor="..." 처럼 넣어준 값들)
    const vendor = selectedOption.getAttribute('data-vendor');
    const model = selectedOption.getAttribute('data-model');
    const cateId = selectedOption.getAttribute('data-cate');
    const height = selectedOption.getAttribute('data-height');
    const reqId = selectedOption.value;

    // 4. 폼 필드에 값 채워넣기
    // 제조사
    const vendorInput = document.querySelector('input[name="vendor"]');
    if(vendorInput) vendorInput.value = vendor;

    // 모델명
    const modelInput = document.querySelector('input[name="modelName"]');
    if(modelInput) modelInput.value = model;

    // 높이 (HeightUnit)
    const heightInput = document.querySelector('input[name="heightUnit"]');
    if(heightInput) heightInput.value = height;

    // 카테고리 (SelectBox) 변경
    const cateSelect = document.querySelector('select[name="cateId"]');
    if (cateSelect) {
        cateSelect.value = cateId;
    }

    // 히든 필드(reqId) 채우기 -> 저장 시 상태 변경(APPROVED)을 위해 필수
    const reqField = document.getElementById('reqIdField');
    if(reqField) reqField.value = reqId;

    // 사용자 알림
    alert("신청서 정보가 적용되었습니다.\n나머지 정보(랙 위치, 시리얼, IP)를 확인 후 등록하세요.");
}