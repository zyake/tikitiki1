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
import java.util.ResourceBundle;

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

        ResourceBundle settings = ResourceBundle.getBundle("settings");
        QueryTuningServer server = new QueryTuningServer(settings.getString("server.host"), 80);
        
        Thread warmupThread  = new Thread(new ServerWarmupRunner(server));
        warmupThread.setDaemon(true);
        warmupThread.start();

        server.start();
    }
}
