/* device_form.js - 신청서 불러오기 및 폼 제어 스크립트 */

function loadRequestData(selectObj) {
    const selectedOption = selectObj.options[selectObj.selectedIndex];
    const reqId = selectObj.value; // 선택된 신청서 ID

    // 1. 신청서가 선택되었는지 여부 (선택되면 true -> 잠금 모드)
    const isLocked = (reqId !== "");

    // 헬퍼 함수: 텍스트 입력칸 값 넣기 & 잠금 토글
    const setInput = (name, value) => {
        const input = document.querySelector(`input[name="${name}"]`);
        if (input) {
            if (value !== undefined) input.value = value;

            // 잠금 모드면 readonly 설정, 아니면 해제
            input.readOnly = isLocked;

            // 시각적 효과 (회색 배경)
            if (isLocked) input.classList.add('bg-light');
            else input.classList.remove('bg-light');
        }
    };

    // 헬퍼 함수: 드롭다운(Select) 값 넣기 & 잠금 토글 (Hidden 처리 포함)
    const setSelect = (name, value) => {
        const select = document.querySelector(`select[name="${name}"]`);
        if (select) {
            if (value !== undefined) select.value = value;

            // 드롭다운은 disabled로 잠금
            select.disabled = isLocked;

            if (isLocked) {
                select.classList.add('bg-light');
                // disabled 되면 값이 전송 안 되므로, hidden input을 동적으로 생성
                // (이미 있으면 값만 업데이트, 없으면 생성)
                let hidden = document.querySelector(`input[type="hidden"][name="${name}"]`);
                if (!hidden) {
                    hidden = document.createElement('input');
                    hidden.type = 'hidden';
                    hidden.name = name;
                    select.parentNode.appendChild(hidden);
                }
                hidden.value = select.value;
            } else {
                select.classList.remove('bg-light');
                // 잠금 해제 시 hidden input 제거 (중복 전송 방지)
                const hidden = document.querySelector(`input[type="hidden"][name="${name}"]`);
                if (hidden) hidden.remove();
            }
        }
    };

    // 헬퍼 함수: 텍스트영역(TextArea) 값 넣기 & 잠금
    const setTextarea = (name, value) => {
        const area = document.querySelector(`textarea[name="${name}"]`);
        if (area) {
            if (value !== undefined) area.value = value;
            area.readOnly = isLocked;
            if (isLocked) area.classList.add('bg-light');
            else area.classList.remove('bg-light');
        }
    }

    // 2. data-* 속성 읽어오기
    const company = selectedOption.getAttribute('data-company');
    const companyPhone = selectedOption.getAttribute('data-company-phone');
    const userName = selectedOption.getAttribute('data-username');
    const contact = selectedOption.getAttribute('data-contact');
    const purpose = selectedOption.getAttribute('data-purpose');

    const vendor = selectedOption.getAttribute('data-vendor');
    const model = selectedOption.getAttribute('data-model');
    const cateId = selectedOption.getAttribute('data-cate');
    const height = selectedOption.getAttribute('data-height');
    const cdate = selectedOption.getAttribute('data-cdate');
    const cmonth = selectedOption.getAttribute('data-cmonth');

    // ⭐ [추가] 전력 및 EMS 데이터 읽기
    const power = selectedOption.getAttribute('data-power');
    const ems = selectedOption.getAttribute('data-ems');


    // 3. 값 적용 및 잠금 실행

    // [소유자 정보]
    setInput('companyName', company);
    setInput('companyPhone', companyPhone);
    setInput('userName', userName);
    setInput('contact', contact);

    // [장비 정보]
    setSelect('cateId', cateId);      // 종류
    setInput('vendor', vendor);       // 제조사
    setInput('modelName', model);     // 모델명
    setInput('heightUnit', height);   // 높이

    // ⭐ [추가] 전력 및 EMS 적용
    setInput('powerWatt', power);
    setSelect('emsStatus', ems);


    // [계약 정보]
    setTextarea('description', purpose);
    setInput('contractDate', cdate);
    setSelect('contractMonth', cmonth);

    // [히든 필드 업데이트] (Controller로 넘겨줄 reqId)
    const reqField = document.getElementById('reqIdField');
    if(reqField) reqField.value = reqId;

    // 4. 사용자 알림
    if (isLocked) {
        alert("신청서 내용이 불러와졌습니다.\n데이터 보호를 위해 신청서 관련 정보는 수정할 수 없습니다.\n\n'랙 위치', '시리얼 번호', 'IP'를 입력 후 등록하세요.");
    }
}

// 2. 추가: 랙 선택 시 max 속성 업데이트
/**
 * 랙 선택 시 호출되는 함수: 시작 유닛과 높이의 max 속성을 업데이트
 */
function updateMaxUnit(rackSelect) {
    if (!rackSelect) return;

    const selectedOption = rackSelect.options[rackSelect.selectedIndex];
    if (!selectedOption || selectedOption.value === "") return;

    const totalUnit = selectedOption.getAttribute('data-total-unit');
    const startUnitInput = document.querySelector('input[name="startUnit"]');
    const heightUnitInput = document.querySelector('input[name="heightUnit"]');

    if (totalUnit) {
        if (startUnitInput) startUnitInput.max = totalUnit;
        if (heightUnitInput) heightUnitInput.max = totalUnit;
        console.log("선택된 랙의 최대 유닛: " + totalUnit + "U");
    }
}

// 3. 추가: 페이지 초기화 로직 (HTML에서 호출)
/**
 * 페이지 초기화 로직
 * @param {string} errorMsg - 서버에서 전달된 에러 메시지
 */
function initializeForm(errorMsg) {
    const rackSelect = document.querySelector('select[name="rackId"]');

    // 1. 페이지 로드 시 이미 랙이 선택되어 있다면 max 설정
    if (rackSelect && rackSelect.value) {
        updateMaxUnit(rackSelect);
    }

    // 2. 에러 메시지가 존재하는 경우 처리
    if (errorMsg) {
        const firstInvalid = document.querySelector('.is-invalid');
        if (firstInvalid) {
            firstInvalid.focus();
        }
    }
    // 3. 에러는 없지만 랙을 선택해야 하는 경우 포커스
    else {
        if (rackSelect && !rackSelect.value && !rackSelect.disabled) {
            rackSelect.focus();
        }
    }
}