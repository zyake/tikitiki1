package tikitiki.batch;

import tikitiki.repository.VisitLogRepository;
import tikitiki.util.CacheManager;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class CacheUpdateTask extends TimerTask {

    private final String jdbcUrl;

    public CacheUpdateTask(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void start() {
        Timer timer = new Timer(true);
        timer.schedule(this, 24 * 3600, 24 * 3600);
    }

    @Override
    public void run() {
        System.out.println("start batch..." + new Date());

        try (VisitLogRepository repository = newRepository()) {
            Map<String, Object> cacheMap = new HashMap<>();
            for ( int i = 1 ; i <= 100; i ++ ) {
                List<Map<String, Object>> resultSet = repository.queryByCondition(i);
                String outputHTML = renderHTML(resultSet);
                cacheMap.put(Integer.toString(i), outputHTML);
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
        return "<!doctype html>\n" +
                "<html lang=\"ja\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>AACon1</title>\n" +
                "    <link rel=\"stylesheet\" href=\"/lib/css/style.css\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<header>\n" +
                "    <h1>検索結果</h1>\n" +
                "</header>\n" +
                "\n" +
                "<article>\n" +
                "\n" +
                "</article>\n" +
                "\n" +
                "<table class=\"tbl\">\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "<th>id</th><th>user.name</th><th>hotel.name</th><th>date</th>" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                renderResponses(resultSet) +
                "    </tbody>\n" +
                "\n" +
                "</table>\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    private String renderResponses(List<Map<String, Object>> resultSet) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map resultRow : resultSet) {
            stringBuilder
                    .append("<tr>")
                    .append("<td>").append(resultRow.get("id")).append("</td>")
                    .append("<td>").append(resultRow.get("user_name")).append("</td>")
                    .append("<td>").append(resultRow.get("hotel_name")).append("</td>")
                    .append("<td>").append(resultRow.get("date")).append("</td>")
                    .append("</tr>");
        }
        return stringBuilder.toString();
    }

}
