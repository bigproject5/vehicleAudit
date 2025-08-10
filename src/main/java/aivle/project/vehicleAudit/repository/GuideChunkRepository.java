package aivle.project.vehicleAudit.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GuideChunkRepository {

    private final JdbcTemplate jdbc;

    @Data @AllArgsConstructor
    public static class Row {
        public String docName;
        public String section;
        public String version;
        public String content;
        public double score;
    }

    public List<Row> searchByProcess(String process, double[] vec, int topk) {
        String v = toVectorLiteral(vec);
        String sql = """
SELECT doc_name, section, version, content,
       1 - (embedding <=> (?::vector)) AS score
FROM guide_chunks
WHERE process_code = ?
ORDER BY embedding <=> (?::vector)
LIMIT ?
""";
        return jdbc.query(sql, ps -> { ps.setString(1, v); ps.setString(2, process); ps.setString(3, v); ps.setInt(4, topk); },
                (rs, i) -> new Row(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5)));
    }

    public void insertChunk(String process, String doc, String section, String version, String content, double[] vec) {
        String v = toVectorLiteral(vec);
        String sql = "INSERT INTO guide_chunks(process_code, doc_name, section, version, content, keywords, embedding) VALUES (?,?,?,?,?,NULL, ?::vector)";
        jdbc.update(sql, process, doc, section, version, content, v);
    }

    private String toVectorLiteral(double[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) { if (i>0) sb.append(','); sb.append(v[i]); }
        sb.append(']');
        return sb.toString();
    }
}
