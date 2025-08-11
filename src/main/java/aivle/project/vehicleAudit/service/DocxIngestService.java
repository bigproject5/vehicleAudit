package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.repository.GuideChunkRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocxIngestService {

    private final GuideChunkRepository repo;
    private final EmbeddingClient embeddingClient;

    public int ingestDocx(String process, String version, MultipartFile file) throws Exception {
        List<Row> rows = parse(file.getInputStream());
        int count = 0;
        int idx = 0;
        for (Row r : rows) {
            String content =
                    "[증상]" + r.symptom + "\n" +
                            "[원인]" + r.cause + "\n" +
                            "[조치]" + r.counter + "\n" +
                            "[확인]" + r.verify + "\n" +
                            "[공구]" + r.tools + "\n" +
                            "[부품]" + r.parts;
            double[] vec = embeddingClient.embed(content);
            repo.insertChunk(process, file.getOriginalFilename(), "table#" + (idx++), version, content, vec);
            count++;
        }
        return count;
    }

    private List<Row> parse(InputStream in) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            List<Row> result = new ArrayList<>();
            for (XWPFTable t : doc.getTables()) {
                int start = t.getRows().size() > 1 ? 1 : 0; // 헤더 한 줄 스킵 가정
                for (int i = start; i < t.getNumberOfRows(); i++) {
                    var r = t.getRow(i);
                    var cells = r.getTableCells();
                    Row row = new Row();
                    row.symptom = cells.size() > 0 ? cells.get(0).getText() : "";
                    row.cause   = cells.size() > 1 ? cells.get(1).getText() : "";
                    row.counter = cells.size() > 2 ? cells.get(2).getText() : "";
                    row.verify  = cells.size() > 3 ? cells.get(3).getText() : "";
                    row.tools   = cells.size() > 4 ? cells.get(4).getText() : "";
                    row.parts   = cells.size() > 5 ? cells.get(5).getText() : "";
                    if (!row.isEmpty()) result.add(row);
                }
            }
            return result;
        }
    }

    static class Row {
        String symptom, cause, counter, verify, tools, parts;
        boolean isEmpty() {
            return (symptom+cause+counter+verify+tools+parts).replace(" ","").isEmpty();
        }
    }
}
