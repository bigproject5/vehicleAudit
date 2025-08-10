package aivle.project.vehicleAudit.rest;

import aivle.project.vehicleAudit.rest.dto.RagSuggestRequest;
import aivle.project.vehicleAudit.rest.dto.RagSuggestResponse;
import aivle.project.vehicleAudit.service.DocxIngestService;
import aivle.project.vehicleAudit.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/internal/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;
    private final DocxIngestService ingestService;

    @GetMapping("/health")
    public String health() { return "ok"; }

    @PostMapping("/suggest")
    public RagSuggestResponse suggest(@RequestBody RagSuggestRequest req) {
        return ragService.suggest(req);
    }

    @PostMapping(value = "/admin/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String ingest(@RequestParam("process") String process,
                         @RequestParam("version") String version,
                         @RequestParam("file") MultipartFile file) throws Exception {
        int n = ingestService.ingestDocx(process, version, file);
        return "ingested " + n + " rows";
    }
}
