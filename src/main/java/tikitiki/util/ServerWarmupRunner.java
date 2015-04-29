package tikitiki.util;

import tikitiki.web.QueryTuningServer;

import java.net.URL;

public class ServerWarmupRunner implements Runnable {

    private final QueryTuningServer server;

    public ServerWarmupRunner(QueryTuningServer server) {
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("start warm up...");
        for (int i = 1 ;  i < 100 ; i ++) {
          for (int j = 0 ; j < 10 ; j ++) {
              try {
                  URL url = new URL(server.getURI() + "/tikitiki/query_tuning/" + i);
                  url.getContent();
              } catch (java.io.IOException e) {
                  throw new RuntimeException(e);
              }
          }
        }
        System.out.println("end warm up.");
    }
}