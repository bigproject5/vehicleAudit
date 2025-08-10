package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.event.TestStartedEventDTO;
import aivle.project.vehicleAudit.event.WorkerTaskCompletedEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleAuditEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTestStartedEvent(TestStartedEventDTO event) {
        kafkaTemplate.send("test-started", event);
    }
    public void sendWorkerTaskCompletedEvent(WorkerTaskCompletedEventDTO event) {
        kafkaTemplate.send("worker-task-completed", event);
    }
}
