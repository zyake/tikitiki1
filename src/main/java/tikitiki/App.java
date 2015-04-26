package tikitiki;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.eclipse.jetty.server.Server;
import tikitiki.batch.CacheUpdateTask;
import tikitiki.repository.VisitLogRepository;
import tikitiki.util.ServerWarmupRunner;
import tikitiki.web.QueryTuningHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

public class App {

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

        InetSocketAddress socketAddress = InetSocketAddress.createUnresolved("aacon1.aa-dev.com", 80);
        Server server = new Server(socketAddress);
        server.setHandler(new QueryTuningHandler());
        server.start();

        new ServerWarmupRunner(server).run();
    }
}
