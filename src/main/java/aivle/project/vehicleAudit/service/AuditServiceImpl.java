package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.*;
import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import aivle.project.vehicleAudit.domain.enumerate.InspectionType;
import aivle.project.vehicleAudit.event.TestStartedEventDTO;
import aivle.project.vehicleAudit.event.WorkerTaskCompletedEventDTO;
import aivle.project.vehicleAudit.repository.AuditRepository;
import aivle.project.vehicleAudit.repository.InspectionRepository;
import aivle.project.vehicleAudit.repository.TaskRepository;
import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aivle.project.vehicleAudit.domain.Inspection;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final InspectionRepository inspectionRepository;
    private final TaskRepository taskRepository;
    private final VehicleAuditEventProducer eventProducer;

    @Override
    @Transactional
    public Audit create(Audit audit, List<Inspection> inspections) {
        audit.init();
        inspections.forEach(inspection -> {
            inspection.init();
            inspection.addToAudit(audit);  // Inspection에 Audit 설정
        });
        ///////////////////////////////////////////////
        Audit savedAudit = auditRepository.save(audit);
        inspections.forEach(inspection -> {
            TestStartedEventDTO event = new TestStartedEventDTO(
                    savedAudit.getId(),
                    savedAudit.getModel(),
                    savedAudit.getLineCode(),
                    inspection.getType().name(),
                    inspection.getCollectDataPath()
            );
            eventProducer.sendTestStartedEvent(event);
        });

        return savedAudit;
        //////////////////////////////////////////////////
    }


    @Override
    @Transactional
    public Page<Audit> findAll(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Audit findById(Long id) {
        return auditRepository.findWithInspectionsById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 Audit ID를 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public Page<Inspection> findInspectionsByWorkerId(Long workerId, Pageable pageable) {
        return inspectionRepository.findByTaskWorkerId(workerId, pageable);
    }

    @Override
    @Transactional
    public Inspection startTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));

        inspection.startTask(workerId, workerName);

        return inspectionRepository.save(inspection);
    }

    //TODO: 작업 완료 시 Audit의 모든 Inspection이 완료되었는지 확인하고, 모든 Inspection이 완료되었다면 Audit의 상태를 변경하는 로직 추가 필요
    @Override
    @Transactional
    public Inspection finishTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        Inspection inspection = inspectionRepository.findWithTaskById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
        inspection.finishTask(workerId);
        /// ////////////////////////////////////////
        WorkerTaskCompletedEventDTO event = new WorkerTaskCompletedEventDTO(
                inspection.getTask().getWorkerName(),
                inspection.getTask().getResolve(),
                inspection.getAudit().getId(),
                inspection.getId(),
                inspection.getTask().getWorkerId(),
                inspection.getTask().getStartedAt(),
                inspection.getTask().getEndedAt(),
                inspection.getType().name()
        );

        eventProducer.sendWorkerTaskCompletedEvent(event);
        ///  /////////////////////////////////////////
        return inspectionRepository.save(inspection);
    }



    @Override
    @Transactional
    public Inspection findByInspectionId(Long inspectionId) {
        return inspectionRepository.findWithTaskById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
    }

    @Override
    @Transactional
    public Inspection updateTaskResolve(Long inspectionId, Long workerId, String resolve) {
        Inspection inspection = inspectionRepository.findWithTaskById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
        inspection.modifyResolve(workerId, resolve);
        return inspection;
    }

    // 테스트용 진단 완료 처리 메서드 진단 이상이 있는 것으로 가정
    @Override
    @Transactional
    public Inspection diagnosisComplete(Long inspectionId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
        inspection.setAiSuggestion("AI 제안");
        inspection.setResultDataPath("result/data/path");
        inspection.setDiagnosisResult("진단 결과");
        inspection.setStatus(InspectionStatus.ABNORMAL);
        return inspectionRepository.save(inspection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Inspection> searchInspections(InspectionType inspectionType, Long workerId, InspectionStatus status, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        QInspection inspection = QInspection.inspection;
        QTask task = QTask.task;

        // inspectionType 필터링
        if (inspectionType != null) {
            builder.and(inspection.inspectionType.eq(inspectionType));
        }

        // status 필터링
        if (status != null) {
            builder.and(inspection.status.eq(status));
        }

        // workerId 필터링 (task 조인 필요)
        if (workerId != null) {
            builder.and(inspection.task.workerId.eq(workerId));
        }

        return inspectionRepository.findAll(builder, pageable);
    }
}
