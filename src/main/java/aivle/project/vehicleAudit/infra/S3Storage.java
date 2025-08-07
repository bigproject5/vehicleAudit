package aivle.project.vehicleAudit.infra;

import aivle.project.vehicleAudit.domain.Inspection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
public class S3Storage implements FileStorage{

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String storeCollectFile(Inspection inspection) throws Exception {
        if (inspection.getCollectDataFile() == null || inspection.getCollectDataFile().isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다.");
        }

        // audit ID와 inspectionType을 이용한 파일명 생성
        String originalFilename = inspection.getCollectDataFile().getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";

        // audit의 id를 폴더로, inspectionType을 파일명으로 사용
        String fileName = String.format("%d/%s_%s%s",
            inspection.getAudit().getId(),
            inspection.getInspectionType().name().toLowerCase(),
            UUID.randomUUID().toString(),
            extension);

        try {
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(inspection.getCollectDataFile().getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(
                    inspection.getCollectDataFile().getInputStream(),
                    inspection.getCollectDataFile().getSize()));

            // S3 URL 반환
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);

        } catch (IOException e) {
            throw new Exception("파일 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
