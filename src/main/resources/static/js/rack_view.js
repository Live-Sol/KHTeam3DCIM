let currentDeviceId = null; // 현재 열려있는 장비 ID 저장용

// ============================
// 장비 모달 창 열기
// ============================
function showDeviceModal(deviceId) {
    currentDeviceId = deviceId; // ID 저장

    fetch('/api/devices/' + deviceId)
        .then(response => response.json())
        .then(data => {
            document.getElementById('modalVendor').innerText = data.vendor;
            document.getElementById('modalModel').innerText = data.modelName;
            document.getElementById('modalSerial').innerText = data.serialNum;
            document.getElementById('modalIp').innerText = data.ipAddr;
            updateStatusUI(data.status);
            document.getElementById('modalEditBtn').href = '/devices/' + data.id + '/edit';

            // ====================
            // 날짜 및 만료일 계산 로직
            // ====================
            const dateElem = document.getElementById('modalContractDate');
            const expiryElem = document.getElementById('modalExpiry');

            if (data.contractDate) {
                // 입고일 표시
                dateElem.innerText = data.contractDate;

                // 만료일 계산 (입고일 + 개월수)
                if (data.contractMonth) {
                    const startDate = new Date(data.contractDate);
                    // 개월 수 더하기
                    startDate.setMonth(startDate.getMonth() + data.contractMonth);

                    // YYYY-MM-DD 형식으로 변환
                    const expiryStr = startDate.toISOString().split('T')[0];

                    expiryElem.innerText = `+${data.contractMonth}개월 (~${expiryStr})`;
                } else {
                    expiryElem.innerText = "-";
                }
            } else {
                dateElem.innerText = "-";
                expiryElem.innerText = "-";
            }
            // ===============================================

            // QR 코드 생성
            const qrContainer = document.getElementById("qrcode");
            qrContainer.innerHTML = ""; // 기존 QR 비우기 (필수!)

            // QR에 담을 내용: JSON 형태의 핵심 정보 (실무에선 보통 장비 조회 URL이나 시리얼번호를 넣습니다)
            const qrData = `ID:${data.id}\nSN:${data.serialNum}\nIP:${data.ipAddr}`;

            // 라이브러리 사용해 QR 그리기
            new QRCode(qrContainer, {
                text: qrData,
                width: 100,
                height: 100,
                colorDark : "#000000",
                colorLight : "#ffffff",
                correctLevel : QRCode.CorrectLevel.H
            });

            const myModal = new bootstrap.Modal(document.getElementById('deviceModal'));
            myModal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('정보 로딩 실패');
        });
}

// ============================
// 전원 버튼 클릭 시 실행
// ============================
function togglePower() {
    if(!currentDeviceId) return;
    if(!confirm("장비의 전원 상태를 변경하시겠습니까?")) return;

    // 👇 HTML 머리(head)에 심어둔 도장을 꺼내옵니다.
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch('/api/devices/' + currentDeviceId + '/toggle-status', {
        method: 'POST',
        headers: {
            // 👇 헤더에 도장을 같이 붙여서 보냅니다!
            [header]: token
        }
    })
    .then(response => {
        if (response.ok) return response.text();
        throw new Error("전원 변경 실패"); // 에러 처리
    })
    .then(newStatus => {
        alert("전원 상태가 변경되었습니다.");
        location.reload();
    })
    .catch(error => {
        console.error(error);
        alert("오류가 발생했습니다.");
    });
}

// UI 상태 업데이트 (중복 제거용 함수)
function updateStatusUI(status) {
    const statusSpan = document.getElementById('modalStatus');
    const powerBtn = document.getElementById('modalPowerBtn');

    if (status === 'RUNNING') {
        statusSpan.innerHTML = '<span class="badge bg-success">가동중 (ON)</span>';
        // 버튼은 '끄기' 모양으로
        powerBtn.className = 'btn btn-outline-danger';
        powerBtn.innerHTML = '<i class="bi bi-power"></i> 전원 끄기';
    } else {
        statusSpan.innerHTML = '<span class="badge bg-secondary">중지됨 (OFF)</span>';
        // 버튼은 '켜기' 모양으로
        powerBtn.className = 'btn btn-outline-success';
        powerBtn.innerHTML = '<i class="bi bi-power"></i> 전원 켜기';
    }
}