package aivle.project.vehicleAudit.rest;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.domain.Inspection;
import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import aivle.project.vehicleAudit.rest.dto.AuditCreateDTO;
import aivle.project.vehicleAudit.rest.dto.AuditDTO;
import aivle.project.vehicleAudit.rest.dto.AuditSummaryDTO;
import aivle.project.vehicleAudit.rest.dto.InspectionDTO;
import aivle.project.vehicleAudit.rest.dto.InspectionSummaryDTO;
import aivle.project.vehicleAudit.rest.dto.ResponseDTO;
import aivle.project.vehicleAudit.rest.mapper.AuditMapper;
import aivle.project.vehicleAudit.rest.mapper.InspectionMapper;
import aivle.project.vehicleAudit.service.AuditService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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

    @GetMapping("/audits")
    public ResponseEntity<ResponseDTO<Page<AuditSummaryDTO>>> getAllAudits(Pageable pageable) {
        log.info("Received request to get all audits with pageable: {}", pageable);
        Page<Audit> audits = auditService.findAll(pageable);
        Page<AuditSummaryDTO> auditDTOs = audits.map(auditMapper::toSummaryDto);
        return ResponseEntity.ok().body(ResponseDTO.success(auditDTOs));
    }

    @GetMapping("/audits/{auditId}")
    public ResponseEntity<ResponseDTO<AuditDTO>> getAuditById(@PathVariable Long auditId) {
        log.info("Received request to get audit by ID: {}", auditId);
        Audit audit = auditService.findById(auditId);
        return ResponseEntity.ok().body(ResponseDTO.success(auditMapper.toDto(audit)));
    }

    @GetMapping("/inspections")
    public ResponseEntity<ResponseDTO<Page<InspectionSummaryDTO>>> getInspectionsByWorkerId(@RequestParam Long workerId, Pageable pageable) {
        log.info("Received request to get inspections by worker ID: {}", workerId);
        Page<Inspection> inspections = auditService.findInspectionsByWorkerId(workerId, pageable);
        return ResponseEntity.ok().body(ResponseDTO.success(inspections.map(inspectionMapper::toSummaryDto)));
    }

    @GetMapping("/inspections/{inspectionId}")
    public ResponseEntity<ResponseDTO<InspectionDTO>> getInspectionById(@PathVariable Long inspectionId) {
        log.info("Received request to get inspection by ID: {}", inspectionId);
        Inspection inspection = auditService.findByInspectionId(inspectionId);
        return ResponseEntity.ok().body(ResponseDTO.success(inspectionMapper.toDto(inspection)));
    }


    @PatchMapping("/inspections/{inspectionId}")
    public ResponseEntity<ResponseDTO<?>> startTaskOnInspection(
            @PathVariable Long inspectionId,
            @RequestHeader Long workerId,
            @RequestHeader String workerName,
            @RequestBody InspectionDTO inspectionDTO
    ) {
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
}