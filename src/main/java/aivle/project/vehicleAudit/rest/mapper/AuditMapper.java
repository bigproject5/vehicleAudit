package aivle.project.vehicleAudit.rest.mapper;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.rest.dto.AuditCreateDTO;
import aivle.project.vehicleAudit.rest.dto.AuditDTO;
import aivle.project.vehicleAudit.rest.dto.AuditSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InspectionMapper.class)
public interface AuditMapper {

    Audit toEntity(AuditCreateDTO dto);

    AuditDTO toDto(Audit audit);

    AuditSummaryDTO toSummaryDto(Audit audit);
}
