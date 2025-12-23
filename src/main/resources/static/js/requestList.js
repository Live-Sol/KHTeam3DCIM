function rejectRequest(requestId) {
    Swal.fire({
        title: '반려 사유 입력',
        input: 'textarea',
        inputPlaceholder: '반려 사유를 입력하세요',
        showCancelButton: true,
        confirmButtonText: '반려',
        cancelButtonText: '취소',
        confirmButtonColor: '#dc3545',
        inputValidator: (value) => {
            if (!value) {
                return '반려 사유는 필수입니다';
            }
        }
    }).then((result) => {
        if (result.isConfirmed) {
            submitReject(requestId, result.value);
        }
    });
}

function submitReject(requestId, reason) {
    var csrfMeta = document.querySelector('meta[name="_csrf"]');
    var csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    var headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    };

    if (csrfMeta && csrfHeaderMeta) {
        headers[csrfHeaderMeta.content] = csrfMeta.content;
    }

    fetch('/requests/' + requestId + '/reject', {
        method: 'POST',
        headers: headers,
        body: 'reason=' + encodeURIComponent(reason)
    }).then(function () {
        Swal.fire({
            icon: 'success',
            title: '반려 완료',
            timer: 1200,
            showConfirmButton: false
        }).then(function () {
            location.reload();
        });
    });
}
