package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.dto.Request.RequestDTO;
import com.example.KHTeam3DCIM.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public List<Request> findWaitingRequests(String keyword, String emsStatus) {
        // 1. 빈 문자열 처리 (검색어가 없으면 null로)
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        // 2. "전체" 선택 시 null로 처리하여 필터 해제
        if ("ALL".equals(emsStatus) || (emsStatus != null && emsStatus.trim().isEmpty())) {
            emsStatus = null;
        }

        return requestRepository.searchWaitingRequests(keyword, emsStatus);
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
    @Transactional // 데이터 일관성을 위해 추가 권장
    public void approveRequest(Long reqId, Long rackId, Integer startUnit) {
        // 1. 신청서 정보 가져오기
        Request request = findById(reqId);

        // 2. 신청서에 담긴 회원(Member) 찾기
        Member requester = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new IllegalStateException("신청자 정보를 찾을 수 없습니다. (ID: " + request.getMemberId() + ")"));

        // 3. 랙 정보 가져오기
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 랙을 찾을 수 없습니다. ID: " + rackId));

        // 3-1. Request의 String cateId를 사용하여 실제 Category 엔티티 조회
        Category category = categoryRepository.findById(request.getCateId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리 ID입니다: " + request.getCateId()));

        // 3-2. 랙 높이 초과 검증
        int heightUnit = request.getHeightUnit();
        int endUnit = startUnit + heightUnit - 1;

        if (endUnit > rack.getTotalUnit()) {
            throw new IllegalStateException(
                    "선택한 위치에 장비를 배치할 수 없습니다. " +
                            "(랙 최대 높이: " + rack.getTotalUnit() + "U, " +
                            "요청 범위: " + startUnit + " ~ " + endUnit + "U)"
            );
        }

        // 3-3. 기존 장비와 위치 충돌 검사
        boolean overlap = deviceRepository.existsOverlappingDevice(rack, startUnit, endUnit);

        if (overlap) {
            throw new IllegalStateException(
                    "해당 랙의 " + startUnit + "U ~ " + endUnit +
                            "U 구간에는 이미 장비가 배치되어 있습니다."
            );
        }

        // ⭐ [추가] 시리얼 번호 생성 (기존 임시값 대신 사용)
        // 예: StarRoot_오늘날짜_신청ID (예: SR_231224_105)
        String generatedSerial = "SR_" +
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd")) +
                "_" + reqId;

        // 4. 장비 객체 생성 (신청서 데이터를 장비로 이식)
        Device device = Device.builder()
                .member(requester)
                .rack(rack)
                .category(category)
                .vendor(request.getVendor())
                .modelName(request.getModelName())
                .serialNum(generatedSerial) // ⭐ [수정] 생성한 시리얼 번호 주입
                .startUnit(startUnit)
                .heightUnit(request.getHeightUnit())
                .powerWatt(request.getPowerWatt())
                .emsStatus(request.getEmsStatus())
                .status("OFF")
                .companyName(request.getCompanyName())
                .companyPhone(request.getCompanyPhone())
                .userName(request.getUserName())
                .contact(request.getContact())
                .description(request.getPurpose())
                .contractDate(request.getStartDate())
                .contractMonth(request.getTermMonth())
                .build();

        // 5. 장비 저장
        deviceRepository.save(device);

        // 6. 신청서 상태 완료 처리 및 시리얼 번호 기록
        request.setStatus("APPROVED");
        request.setSerialNum(generatedSerial); // ⭐ [추가] Request 엔티티에도 시리얼 번호 저장
    }

    // 8. 요청 삭제 또는 숨김 처리 (사용자용)
    public void processRemoveOrHide(Long id) {
        Request request = findById(id);

        if ("APPROVED".equals(request.getStatus())) {
            // 승인된 건은 숨김 처리 (Entity에 @Data가 있으므로 바로 사용 가능)
            request.setHidden(true);
            // @Transactional이 있으므로 별도의 save 호출 없이도 반영됨
        } else {
            // 대기 중/반려 건은 실제 삭제 (기존에 구현된 deleteRequest 활용)
            deleteRequest(id);
        }
    }
    // 내 신청 이력 중 숨겨지지 않은 것만 조회
    @Transactional(readOnly = true)
    public List<Request> findActiveRequests(String memberId) {
        return requestRepository.findActiveRequestsByMemberId(memberId);
    }
    // 숨김 내역 조회
    @Transactional(readOnly = true)
    public List<Request> findHiddenRequests(String memberId) {
        return requestRepository.findHiddenRequestsByMemberId(memberId);
    }

    // 숨김 내역 복구
    public void restoreRequest(Long id, String memberId) {
        requestRepository.restoreRequest(id, memberId);
    }

    // [수정] 페이징 + 검색 + 정렬 기능이 통합된 메서드
    @Transactional(readOnly = true)
    public Page<Request> findMyRequestsPaged(String memberId, String keyword, String sort, String sortDir, Pageable pageable) {

        // 1. 정렬(Sort) 객체 동적 생성
        // 컨트롤러에서 sort 파라미터가 넘어왔을 경우 (예: vendor, startDate 등)
        if (sort != null && !sort.isEmpty()) {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

            // 기존 pageable 정보에 사용자가 선택한 정렬 조건을 덮어씌움
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(direction, sort)
            );
        }

        // 2. 검색어 가공 (빈 문자열일 경우 null로 처리)
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        // 3. 리포지토리 호출 (검색어와 새로운 pageable 전달)
        // 아래 2번 항목에서 만들 리포지토리 메서드를 호출합니다.
        return requestRepository.findMyActiveRequestsWithSearch(memberId, keyword, pageable);
    }
    @Transactional(readOnly = true)
    public Page<Request> findHiddenRequestsPaged(String memberId, Pageable pageable) {
        return requestRepository.findHiddenRequestsByMemberIdPaged(memberId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Request> findHiddenRequestsPaged(String memberId, String keyword, String sort, String sortDir, Pageable pageable) {

        // 1. 정렬 객체 조립
        if (sort != null && !sort.isEmpty()) {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(direction, sort)
            );
        }

        // 2. 검색어 처리
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        // 3. 숨김 내역 전용 검색 리포지토리 호출
        return requestRepository.findMyHiddenRequestsWithSearch(memberId, keyword, pageable);
    }
}