package aivle.project.vehicleAudit.rest;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.domain.Inspection;
import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import aivle.project.vehicleAudit.domain.enumerate.InspectionType;
import aivle.project.vehicleAudit.rest.dto.AuditCreateDTO;
import aivle.project.vehicleAudit.rest.dto.AuditDTO;
import aivle.project.vehicleAudit.rest.dto.AuditManualDTO;
import aivle.project.vehicleAudit.rest.dto.AuditSummaryDTO;
import aivle.project.vehicleAudit.rest.dto.InspectionDTO;
import aivle.project.vehicleAudit.rest.dto.InspectionSummaryDTO;
import aivle.project.vehicleAudit.rest.dto.ResponseDTO;
import aivle.project.vehicleAudit.rest.mapper.AuditMapper;
import aivle.project.vehicleAudit.rest.mapper.InspectionMapper;
import aivle.project.vehicleAudit.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicleaudit")
@Slf4j
public class AuditController {
    private final AuditService auditService;
    private final AuditMapper auditMapper;
    private final InspectionMapper inspectionMapper;

    @PostMapping("/audits")
    public ResponseEntity<ResponseDTO<AuditDTO>> createAuditsWithPaths(@RequestBody AuditCreateDTO auditCreateDTO) {
        log.info("Received request to create audit with: {}", auditCreateDTO);
        Audit audit = auditMapper.toEntity(auditCreateDTO);
        List<Inspection> inspections = auditCreateDTO.inspections().stream().map(inspectionMapper::toEntity).toList();
        Audit savedAudit = auditService.create(audit, inspections);
        return ResponseEntity.ok().body(ResponseDTO.success(auditMapper.toDto(savedAudit)));
    }

    @PostMapping("/audits/manual")
    public ResponseEntity<ResponseDTO<AuditDTO>> createManualAudit(
            HttpServletRequest request,
            @RequestParam("audit") String auditJson,
            @RequestParam MultiValueMap<String, MultipartFile> files
    ) {
        log.info("=== Request Debug Info ===");
        log.info("Content-Type: {}", request.getContentType());
        log.info("Content-Length: {}", request.getContentLength());
        log.info("Method: {}", request.getMethod());

        // 요청 헤더 정보
        log.info("--- Request Headers ---");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            log.info("{}: {}", headerName, request.getHeader(headerName));
        });

        log.info("--- Audit JSON ---");
        log.info("Audit JSON: {}", auditJson);

        log.info("--- Files Info ---");
        log.info("Files keySet: {}", files.keySet());
        files.forEach((key, valueList) -> {
            valueList.forEach(file -> {
                log.info("File - Key: '{}', Name: '{}', OriginalFilename: '{}', Size: {}, ContentType: '{}'",
                        key, file.getName(), file.getOriginalFilename(), file.getSize(), file.getContentType());
            });
        });
        log.info("=== End Request Debug ===");

        // JSON 문자열을 AuditManualDTO로 파싱
        AuditManualDTO auditManualDTO;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            auditManualDTO = objectMapper.readValue(auditJson, AuditManualDTO.class);
            log.info("Successfully parsed audit JSON: {}", auditManualDTO);
        } catch (Exception e) {
            log.error("Failed to parse audit JSON: {}", auditJson, e);
            throw new IllegalArgumentException("Invalid audit JSON format: " + e.getMessage(), e);
        }

        log.info("Received request to create manual audit with audit: {}", auditManualDTO);
        Audit audit = auditMapper.toEntity(auditManualDTO);
        List<Inspection> inspections = new ArrayList<>();
        files.forEach((key, value) -> {
            if (value != null && !value.isEmpty() && !key.equals("audit")) {
                log.info("Processing file for key: {}", key);
                Inspection withFile = Inspection.createWithFile(InspectionType.valueOf(key), value.getFirst());
                inspections.add(withFile);
            }
        });
        Audit savedAudit = auditService.createWithFiles(audit, inspections);
        log.info("Manual audit created successfully with ID: {}", savedAudit.getId());
        return ResponseEntity.ok().body(ResponseDTO.success(auditMapper.toDto(savedAudit)));
    }

    @GetMapping("/audits")
    public ResponseEntity<ResponseDTO<Page<AuditSummaryDTO>>> getAllAudits(Pageable pageable) {
        log.info("Received request to get all audits with pageable: {}", pageable);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        Page<Audit> audits = auditService.findAll(sortedPageable);
        Page<AuditSummaryDTO> auditDTOs = audits.map(auditMapper::toSummaryDto);
        return ResponseEntity.ok().body(ResponseDTO.success(auditDTOs));
    }

    @GetMapping("/audits/{auditId}")
    public ResponseEntity<ResponseDTO<AuditDTO>> getAuditById(@PathVariable Long auditId) {
        log.info("Received request to get audit by ID: {}", auditId);
        Audit audit = auditService.findById(auditId);
        return ResponseEntity.ok().body(ResponseDTO.success(auditMapper.toDto(audit)));
    }

    @GetMapping("/inspections/{inspectionId}")
    public ResponseEntity<ResponseDTO<InspectionDTO>> getInspectionById(@PathVariable Long inspectionId) {
        log.info("Received request to get inspection by ID: {}", inspectionId);
        Inspection inspection = auditService.findByInspectionId(inspectionId);
        return ResponseEntity.ok().body(ResponseDTO.success(inspectionMapper.toDto(inspection)));
    }

    @PatchMapping("/inspections/{inspectionId}")
    public ResponseEntity<ResponseDTO<?>> updateInspection(
            @PathVariable Long inspectionId,
            @RequestHeader(name = "X-User-Id") Long workerId,
            @RequestHeader(name = "X-User-Name") String encodedName,
            @RequestBody InspectionDTO inspectionDTO
    ) {
        String workerName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
        log.info("Received request to start task on inspection ID: {} by worker name: {} with inspection: {}",
                inspectionId, workerName, inspectionDTO);
        if (inspectionDTO.status() != null) {
            if (inspectionDTO.status().equals(InspectionStatus.IN_ACTION.name())) {
                Inspection inspection = auditService.startTaskOnInspection(inspectionId, workerId, workerName);
                return ResponseEntity.ok().body(ResponseDTO.success(inspectionMapper.toDto(inspection)));
            }
            if (inspectionDTO.status().equals(InspectionStatus.COMPLETED.name())) {
                Inspection inspection = auditService.finishTaskOnInspection(inspectionId, workerId, workerName);
                return ResponseEntity.ok().body(ResponseDTO.success(inspectionMapper.toDto(inspection)));
            }
        }
        if (inspectionDTO.resolve() != null) {
            Inspection inspection = auditService.updateTaskResolve(inspectionId, workerId, inspectionDTO.resolve());
            return ResponseEntity.ok().body(ResponseDTO.success(inspectionMapper.toDto(inspection)));
        }
        throw new IllegalArgumentException("Invalid request: status or resolve must be provided");
    }

    //Test용 API 진단 완료 처리
    @PatchMapping("/inspections/{inspectionId}/diagnosis-complete")
    public ResponseEntity<ResponseDTO<InspectionDTO>> completeDiagnosis(
            @PathVariable Long inspectionId
    ) {
        log.info("Received request to complete diagnosis for inspection ID: {}", inspectionId);
        Inspection inspection = auditService.diagnosisComplete(inspectionId);
        return ResponseEntity.ok().body(ResponseDTO.success(inspectionMapper.toDto(inspection)));
    }

    @GetMapping("/inspections")
    public ResponseEntity<ResponseDTO<Page<InspectionSummaryDTO>>> searchInspections(
            @RequestParam(required = false) InspectionType inspectionType,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) InspectionStatus status,
            Pageable pageable
    ) {
        log.info("Received request to search inspections with type: {}, workerId: {}, status: {}",
                inspectionType, workerId, status);

        // inspectionId 기준 최신순 정렬 적용
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<Inspection> inspections = auditService.searchInspections(inspectionType, workerId, status,
                sortedPageable);
        return ResponseEntity.ok().body(ResponseDTO.success(inspections.map(inspectionMapper::toSummaryDto)));
    }
}
