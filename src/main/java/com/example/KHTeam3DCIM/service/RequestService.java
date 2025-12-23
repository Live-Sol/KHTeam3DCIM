package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.dto.Request.RequestDTO;
import com.example.KHTeam3DCIM.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestService {

    private final RequestRepository requestRepository;
    private final DeviceRepository deviceRepository;
    private final MemberRepository memberRepository;
    private final RackRepository rackRepository;
    private final CategoryRepository categoryRepository;

    // 1. 신청서 저장
    public void saveRequest(RequestDTO dto) {
        Request request = dto.toEntity();
        requestRepository.save(request);
    }

    // 2. 대기 중인 신청 목록 조회 (관리자용)
    @Transactional(readOnly = true)
    public List<Request> findWaitingRequests() {
        return requestRepository.findByStatusOrderByReqDateDesc("WAITING");
    }

    // 3. [추가] 내 신청 이력 조회 (사용자용)
    @Transactional(readOnly = true)
    public List<Request> findMyRequests(String memberId) {
        return requestRepository.findByMemberIdOrderByReqDateDesc(memberId);
    }

    // 4. 단일 신청서 상세 조회
    @Transactional(readOnly = true)
    public Request findById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청서가 존재하지 않습니다. ID: " + id));
    }

    // 5. 상태 업데이트
    public void updateStatus(Long id, String status) {
        Request request = findById(id);
        request.setStatus(status);
    }

    // 6. 요청 삭제
    public void deleteRequest(Long reqId) {
        if (!requestRepository.existsById(reqId)) {
            throw new IllegalArgumentException("삭제할 신청서가 없습니다.");
        }
        requestRepository.deleteById(reqId);
    }

    // 반려 사유
    public void rejectRequest(Long id, String reason) {
        Request request = findById(id);
        request.setStatus("REJECTED");
        request.setRejectReason(reason);
    }

    // [7] 입고 승인 및 장비 등록 로직
    public void approveRequest(Long reqId, Long rackId, Integer startUnit) {
        // 1. 신청서 정보 가져오기
        Request request = findById(reqId); // 이미 만들어두신 4번 메서드 활용

        // 2. 신청서에 담긴 회원(Member) 찾기
        // request.getMemberId()가 String이므로 memberRepository에서 찾아야 함
        // [기존 IllegalArgumentException 대신 IllegalStateException 사용 이유]
        // 1. 단순 파라미터 오류가 아니라, DB에 회원이 없는 '시스템 상태'의 문제임을 명시
        // 2. 컨트롤러에서 이 예외만 콕 집어 catch하여 화이트라벨(500에러) 대신 경고창을 띄우기 위함
        Member requester = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new IllegalStateException("신청자 정보를 찾을 수 없습니다. (ID: " + request.getMemberId() + ")"));

        // 3. 랙 정보 가져오기
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 랙을 찾을 수 없습니다. ID: " + rackId));

        // 3-1. Request의 String cateId를 사용하여 실제 Category 엔티티 조회
        Category category = categoryRepository.findById(request.getCateId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리 ID입니다: " + request.getCateId()));
        // 3-2. 랙 높이 초과 검증 (중요)
        int heightUnit = request.getHeightUnit();
        int endUnit = startUnit + heightUnit - 1;

        if (endUnit > rack.getTotalUnit()) {
            throw new IllegalStateException(
                    "선택한 위치에 장비를 배치할 수 없습니다. " +
                            "(랙 최대 높이: " + rack.getTotalUnit() + "U, " +
                            "요청 범위: " + startUnit + " ~ " + endUnit + "U)"
            );
        }

        // 3-3. 기존 장비와 위치 충돌 검사 ⭐
        boolean overlap = deviceRepository.existsOverlappingDevice(
                rack,
                startUnit,
                endUnit
        );

        if (overlap) {
            throw new IllegalStateException(
                    "해당 랙의 " + startUnit + "U ~ " + endUnit +
                            "U 구간에는 이미 장비가 배치되어 있습니다."
            );
        }

        // 4. 장비 객체 생성 (신청서 데이터를 장비로 이식)
        // Device 엔티티에 @Builder가 선언되어 있다면 아래와 같이 작성 가능합니다.
        Device device = Device.builder()
                .member(requester)        // 실제 신청자를 장비 소유자로 연결 ⭐
                .rack(rack)               // 관리자가 지정한 랙 위치
                .category(category)
                .vendor(request.getVendor())
                .modelName(request.getModelName())
                .serialNum("StarRoot_" + reqId) // 시리얼은 입고 후 수정이 필요할 수 있어 임시값 세팅
                .startUnit(startUnit)
                .heightUnit(request.getHeightUnit())
                .powerWatt(request.getPowerWatt())
                .emsStatus(request.getEmsStatus())
                .status("OFF")        // 등록 즉시 가동 상태로 설정
                .companyName(request.getCompanyName())
                .companyPhone(request.getCompanyPhone())
                .userName(request.getUserName())
                .contact(request.getContact())
                .description(request.getPurpose())
                .contractDate(request.getStartDate()) // 신청서의 시작일을 계약일로
                .contractMonth(request.getTermMonth())
                .build();

        // 5. 장비 저장
        deviceRepository.save(device);

        // 6. 신청서 상태 완료 처리
        request.setStatus("APPROVED");
    }

}