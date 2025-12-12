let currentDeviceId = null; // 현재 열려있는 장비 ID 저장용

function showDeviceModal(deviceId) {
    currentDeviceId = deviceId; // ID 저장

    fetch('/api/devices/' + deviceId)
        .then(response => response.json())
        .then(data => {
            document.getElementById('modalVendor').innerText = data.vendor;
            document.getElementById('modalModel').innerText = data.modelName;
            document.getElementById('modalSerial').innerText = data.serialNum;
            document.getElementById('modalIp').innerText = data.ipAddr;

            // 상태 표시 업데이트 함수 호출
            updateStatusUI(data.status);

            document.getElementById('modalEditBtn').href = '/devices/' + data.id + '/edit';

            const myModal = new bootstrap.Modal(document.getElementById('deviceModal'));
            myModal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('정보 로딩 실패');
        });
}

// 전원 버튼 클릭 시 실행
function togglePower() {
    if(!currentDeviceId) return;

    if(!confirm("장비의 전원 상태를 변경하시겠습니까?")) return;

    fetch('/api/devices/' + currentDeviceId + '/toggle-status', {
        method: 'POST'
    })
    .then(response => response.text())
    .then(newStatus => {
        // 알림을 띄우고 -> 확인 누르면 -> 페이지 새로고침
        alert("전원 상태가 변경되었습니다.");
        location.reload(); // 이 한 줄이 '새로고침' 마법입니다!
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