package tikitiki.batch;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import tikitiki.repository.VisitLogRepository;
import tikitiki.util.CacheManager;

import javax.rmi.PortableRemoteObject;
import java.io.StringWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class CacheUpdateTask extends TimerTask {

    private final String jdbcUrl;

    private final VelocityEngine engine;

    public CacheUpdateTask(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
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

        try (VisitLogRepository repository = newRepository()) {
            Map<String, Object> cacheMap = new HashMap<>();
            for ( int i = 1 ; i <= 100; i ++ ) {
                List<Map<String, Object>> resultSet = repository.queryByCondition(i);
                String outputHTML = renderHTML(resultSet);
                // preタグ無視！
                String cleanedOutput = outputHTML.replaceAll("\\r\\n", "");
                cacheMap.put(Integer.toString(i), cleanedOutput.getBytes());
            }
            CacheManager.replaceAtomically(cacheMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("end batch." + new Date());
    }

    private VisitLogRepository newRepository() throws SQLException {
        return new VisitLogRepository(DriverManager.getConnection(jdbcUrl));
    }

    private String renderHTML(List<Map<String, Object>> resultSet) {
        VelocityContext context = new VelocityContext();
        context.put("resultSet", resultSet);

        StringWriter stringWriter = new StringWriter();
        Template template = engine.getTemplate("query_tuning.vm", "UTF-8");
        template.merge(context, stringWriter);
        stringWriter.flush();

        return stringWriter.toString();
    }
}
