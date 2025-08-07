package aivle.project.vehicleAudit.rest.mapper;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.rest.dto.AuditCreateDTO;
import aivle.project.vehicleAudit.rest.dto.AuditDTO;
import aivle.project.vehicleAudit.rest.dto.AuditManualDTO;
import aivle.project.vehicleAudit.rest.dto.AuditSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InspectionMapper.class)
public interface AuditMapper {

    @Mapping(target="model", source="model")
    @Mapping(target="lineCode", source="lineCode")
    @Mapping(target = "inspections", ignore = true)
    Audit toEntity(AuditCreateDTO dto);

    @Mapping(target = "auditId", source = "id")
    @Mapping(target = "testAt", source = "createdAt")
    AuditDTO toDto(Audit audit);

    @Mapping(target = "auditId", source = "id")
    @Mapping(target = "testAt", source = "createdAt")
    AuditSummaryDTO toSummaryDto(Audit audit);

    @Mapping(target="model", source="model")
    @Mapping(target="lineCode", source="lineCode")
    @Mapping(target = "inspections", ignore = true)
    Audit toEntity(AuditManualDTO dto);
}
