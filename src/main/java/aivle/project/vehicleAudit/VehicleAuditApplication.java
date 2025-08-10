package aivle.project.vehicleAudit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VehicleAuditApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleAuditApplication.class, args);
	}

}
