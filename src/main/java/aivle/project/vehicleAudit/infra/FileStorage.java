package aivle.project.vehicleAudit.infra;

import aivle.project.vehicleAudit.domain.Inspection;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    /**
     * 파일을 저장하고, 저장된 파일의 경로를 반환합니다.
     *
     * @param inspection 검사 정보가 포함된 객체
     * @return 저장된 파일의 경로
     */
    String storeCollectFile(Inspection inspection) throws Exception;
}
