package aivle.project.vehicleAudit.rest;

import aivle.project.vehicleAudit.event.AiDiagnosisCompletedEventDTO;
import aivle.project.vehicleAudit.service.AiDiagnosisCompletedEventConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/_debug")
@RequiredArgsConstructor
public class DebugController {

    private final AiDiagnosisCompletedEventConsumer consumer;

    @PostMapping("/diagnosis-completed")
    public String fire(@RequestBody AiDiagnosisCompletedEventDTO e) {
        consumer.handle(e);
        return "ok";
    }
}
