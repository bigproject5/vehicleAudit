package aivle.project.vehicleAudit.rest.mapper;

import aivle.project.vehicleAudit.domain.Inspection;
import aivle.project.vehicleAudit.rest.dto.AuditCreateDTO.InspectionCreateDTO;
import aivle.project.vehicleAudit.rest.dto.InspectionDTO;
import aivle.project.vehicleAudit.rest.dto.InspectionSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InspectionMapper {

    Inspection toEntity(InspectionCreateDTO dto);

    InspectionDTO toDto(Inspection inspection);

    @Mapping(target = "workerId", source = "task.workerId")
    @Mapping(target = "workerName", source = "task.workerName")
    @Mapping(target = "taskStartedAt", source = "task.startedAt")
    @Mapping(target = "taskFinishedAt", source = "task.finishedAt")
    InspectionSummaryDTO toSummaryDto(Inspection inspection);
}
