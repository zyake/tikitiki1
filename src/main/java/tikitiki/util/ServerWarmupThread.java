package tikitiki.util;

import org.eclipse.jetty.server.Server;

import java.net.URL;

public class ServerWarmupThread extends Thread {

    private final Server server;

    public ServerWarmupThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (!server.isStarted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("start warm up...");
        //  一回やれば十分っぽい。
        try {
            URL url = new URL(server.getURI() + "tikitiki/query_tuning/1");
            url.getContent();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("end warm up.");
    }
}
