/* device_form.js */

function loadRequestData(selectObj) {
    const selectedOption = selectObj.options[selectObj.selectedIndex];

    if (selectObj.value === "") {
        return;
    }

    // 1. data-* 속성 읽어오기
    const company = selectedOption.getAttribute('data-company');      // 회사명
    const companyPhone = selectedOption.getAttribute('data-company-phone'); // 회사번호
    const userName = selectedOption.getAttribute('data-username');    // 담당자명
    const contact = selectedOption.getAttribute('data-contact');      // 담당자번호
    const purpose = selectedOption.getAttribute('data-purpose');      // 용도

    const vendor = selectedOption.getAttribute('data-vendor');
    const model = selectedOption.getAttribute('data-model');
    const cateId = selectedOption.getAttribute('data-cate');
    const height = selectedOption.getAttribute('data-height');
    const cdate = selectedOption.getAttribute('data-cdate');
    const cmonth = selectedOption.getAttribute('data-cmonth');
    const reqId = selectedOption.value;

    // 2. 입력 칸에 값 채워넣기

    // [1] 소유자 정보 매핑 (수정됨)
    // 🚑 [수술 완료] selector 이름을 HTML name 속성과 일치시킴
    // input[name="ownerName"] -> input[name="companyName"]
    const ownerInput = document.querySelector('input[name="companyName"]');
    if(ownerInput && company) ownerInput.value = company;

    // [회사 대표 번호]
    const companyPhoneInput = document.querySelector('input[name="companyPhone"]');
    if(companyPhoneInput && companyPhone) companyPhoneInput.value = companyPhone;

    // [담당자 성함]
    const userNameInput = document.querySelector('input[name="userName"]');
    if(userNameInput && userName) userNameInput.value = userName;

    // [담당자 연락처]
    // 🚑 [수술 완료] input[name="contactInfo"] -> input[name="contact"]
    const contactInput = document.querySelector('input[name="contact"]');
    if(contactInput && contact) contactInput.value = contact;

    // [2] 장비 정보 매핑
    const vendorInput = document.querySelector('input[name="vendor"]');
    if(vendorInput) vendorInput.value = vendor;

    const modelInput = document.querySelector('input[name="modelName"]');
    if(modelInput) modelInput.value = model;

    const heightInput = document.querySelector('input[name="heightUnit"]');
    if(heightInput) heightInput.value = height;

    const cateSelect = document.querySelector('select[name="cateId"]');
    if (cateSelect) cateSelect.value = cateId;

    // [3] 계약 및 설명 매핑
    const descInput = document.querySelector('textarea[name="description"]');
    if(descInput && purpose) descInput.value = purpose;

    const dateInput = document.querySelector('input[name="contractDate"]');
    if(dateInput && cdate) dateInput.value = cdate;

    const monthSelect = document.querySelector('select[name="contractMonth"]');
    if(monthSelect && cmonth) monthSelect.value = cmonth;

    // [4] 히든 필드 (reqId) 업데이트
    const reqField = document.getElementById('reqIdField');
    if(reqField) reqField.value = reqId;

    alert("신청서 내용이 불러와졌습니다.\n'랙 위치'와 '시리얼 번호', 'IP'를 입력 후 등록하세요.");
}