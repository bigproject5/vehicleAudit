package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.repository.AuditRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import aivle.project.vehicleAudit.domain.Inspection;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    @Override
    public Audit create(Audit audit) {
        return auditRepository.save(audit);  // createdAt 직접 안 건드림
    }


    @Override
    public Page<Audit> findAll(Pageable pageable) {

        return auditRepository.findAll(pageable);
    }

    @Override
    public Audit findById(Long id) {
        return auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 Audit ID를 찾을 수 없습니다: " + id));
    }

    @Override
    public List<Inspection> findInspectionsByAuditId(Long auditId) {
        return Collections.emptyList();
    }

    @Override
    public Inspection startTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        return null;
    }

    @Override
    public Inspection finishTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        return null;
    }

    @Override
    public Inspection findByInspectionId(Long inspectionId) {

        return null;
    }


}

