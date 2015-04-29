package tikitiki;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tikitiki.batch.CacheUpdateTask;
import tikitiki.repository.VisitLogRepository;
import tikitiki.util.ServerWarmupRunner;
import tikitiki.web.QueryTuningServer;

import java.io.IOException;
import java.io.InputStream;

public class App {

    public App() {
        super();
    }

    public static void main( String[] args ) throws Exception {
        SqlSessionFactory sessionFactory;
        try {
            InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
            sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CacheUpdateTask cacheUpdateTask = new CacheUpdateTask(new VisitLogRepository(sessionFactory.openSession()));
        cacheUpdateTask.run();
        cacheUpdateTask.start();

        String host = args.length == 1 ? args[0] : "localhost";

        QueryTuningServer server = new QueryTuningServer(host, 80);

        new Thread(new ServerWarmupRunner(server)).start();

        server.start();
    }
}
