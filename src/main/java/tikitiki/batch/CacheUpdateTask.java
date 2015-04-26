package tikitiki.batch;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import tikitiki.repository.VisitLogRepository;
import tikitiki.util.CacheManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class CacheUpdateTask extends TimerTask {

    private final VisitLogRepository repository;

    private final VelocityEngine engine;

    public CacheUpdateTask(VisitLogRepository repository) {
        this.repository = repository;
        this.engine = new VelocityEngine();
        Properties properties = new Properties();
        properties.put("resource.loader", "class");
        properties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.put("input.encoding", "UTF-8");
        properties.put("output.encoding", "UTF-8");
        engine.init(properties);
    }

    public void start() {
        long oneDay = 24 * 3600 * 1000;
        Timer timer = new Timer(true);
        timer.schedule(this, oneDay , oneDay);
    }

    @Override
    public void run() {
        System.out.println("start batch..." + new Date());

        try (VisitLogRepository repository = this.repository) {
            Map<String, Object> cacheMap = new HashMap<>();
            for ( int i = 1 ; i <= 100; i ++ ) {
                List<Map<String, Object>> resultSet = repository.queryByCondition(i);
                String outputHTML = renderHTML(resultSet);
                // preタグ無視！ 頭が悪すぎる最適化。
                String cleanedOutput = outputHTML.replaceAll("\\r\\n", "").replaceAll(">\\s+<", "><");
                byte[] compressedOutput = compress(cleanedOutput);
                cacheMap.put(Integer.toString(i), compressedOutput);
            }
            CacheManager.replaceAtomically(cacheMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("end batch." + new Date());
    }

    private byte[] compress(String cleanedOutput) {
        try {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream outputStream = new GZIPOutputStream(arrayOutputStream);
            outputStream.write(cleanedOutput.getBytes());
            outputStream.flush();
            outputStream.close();
            arrayOutputStream.flush();
             return arrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String renderHTML(List<Map<String, Object>> resultSet) {
        // result set内の全項目をエスケープ
        List<Map<String, String>> escapedResultSet = new ArrayList<>();
        for (Map<String, Object> map  : resultSet) {
            Map<String, String> escapedValueMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String escapedValue = StringUtils.replaceEach(entry.getValue().toString(), new String[]{"&", "\"", "<", ">"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;"}).trim();
                escapedValueMap.put(entry.getKey(), escapedValue);
            }
            escapedResultSet.add(escapedValueMap);
        }

        VelocityContext context = new VelocityContext();
        context.put("resultSet", escapedResultSet);

        StringWriter stringWriter = new StringWriter();
        Template template = engine.getTemplate("query_tuning.vm", "UTF-8");
        template.merge(context, stringWriter);
        stringWriter.flush();

        return stringWriter.toString();
    }
}
