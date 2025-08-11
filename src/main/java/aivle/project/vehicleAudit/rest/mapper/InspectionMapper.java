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

    @Mapping(target="type", source = "type")
    @Mapping(target="collectDataPath", source = "collectDataPath")
    Inspection toEntity(InspectionCreateDTO dto);

    @Mapping(target = "inspectionId", source = "id")
    InspectionDTO toDto(Inspection inspection);

    @Mapping(target = "workerId", source = "task.workerId")
    @Mapping(target = "workerName", source = "task.workerName")
    @Mapping(target = "taskStartedAt", source = "task.startedAt")
    @Mapping(target = "taskFinishedAt", source = "task.endedAt")
    @Mapping(target = "inspectionId", source = "id")
    InspectionSummaryDTO toSummaryDto(Inspection inspection);
}
