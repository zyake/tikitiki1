package tikitiki;

import org.eclipse.jetty.server.Server;
import tikitiki.batch.CacheUpdateTask;
import tikitiki.util.ServerWarmupThread;
import tikitiki.web.QueryTuningHandler;

import java.util.ResourceBundle;

public class App {

    public static void main( String[] args ) throws Exception {
        ResourceBundle configs = ResourceBundle.getBundle("configs");

        CacheUpdateTask cacheUpdateTask = new CacheUpdateTask(configs.getString("jdbc.url"));
        cacheUpdateTask.run();
        cacheUpdateTask.start();

        Server server = new Server(80);
        server.setHandler(new QueryTuningHandler());
        server.start();

        new ServerWarmupThread(server).start();
    }
}
