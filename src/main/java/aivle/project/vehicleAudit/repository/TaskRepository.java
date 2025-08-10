package aivle.project.vehicleAudit.repository;
import aivle.project.vehicleAudit.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}