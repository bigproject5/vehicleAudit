package aivle.project.vehicleAudit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id", nullable = false)
    private Long id;

    @Column(name = "worker_id", nullable = false)
    private Long workerId;

    @Column(name = "worker_name", nullable = false)
    private String workerName;

    @CreatedDate
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "resolve", length = 1000, nullable = false)
    private String resolve = "";

    public void allocateWorker(Long workerId, String workerName) {
        this.workerId = workerId;
        this.workerName = workerName;
    }

    public void done(Long workerId) {
        if (!this.workerId.equals(workerId)) {
            throw new IllegalStateException("작업을 완료할 수 없습니다. 작업자가 일치하지 않습니다.");
        }
        this.finishedAt = LocalDateTime.now();
    }
    public void modifyResolve(Long workerId, String resolve) {
        if (!this.workerId.equals(workerId)) {
            throw new IllegalStateException("조치 내용을 수정할 수 없습니다. 작업자가 일치하지 않습니다.");
        }
        if (resolve.isBlank()) {
            throw new IllegalArgumentException("조치 내용을 비워둘 수 없습니다.");
        }
        this.resolve = resolve;
    }
}