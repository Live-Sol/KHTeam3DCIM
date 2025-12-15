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


    // 3. 값 적용 및 잠금 실행

    // [소유자 정보] - 잠금 대상
    setInput('companyName', company);
    setInput('companyPhone', companyPhone);
    setInput('userName', userName);
    setInput('contact', contact);

    // [장비 정보] - 일부 잠금 대상
    setSelect('cateId', cateId);      // 종류 (잠금)
    setInput('vendor', vendor);       // 제조사 (잠금)
    setInput('modelName', model);     // 모델명 (잠금)
    setInput('heightUnit', height);   // 높이 (잠금)

    // ※ 시리얼번호, IP주소, 랙 위치(RackId), 시작위치(StartUnit)는
    //    신청서에 없는 정보이므로 잠그지 않음 (직접 입력해야 함)

    // [계약 정보] - 잠금 대상
    setTextarea('description', purpose);
    setInput('contractDate', cdate);
    setSelect('contractMonth', cmonth);

    // [히든 필드 업데이트]
    const reqField = document.getElementById('reqIdField');
    if(reqField) reqField.value = reqId;

    // 4. 사용자 알림
    if (isLocked) {
        alert("신청서 내용이 불러와졌습니다.\n데이터 보호를 위해 신청서 관련 정보는 수정할 수 없습니다.\n\n'랙 위치', '시리얼 번호', 'IP'를 입력 후 등록하세요.");
    }
}