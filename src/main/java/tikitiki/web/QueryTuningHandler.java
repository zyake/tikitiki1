package tikitiki.web;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import tikitiki.util.CacheManager;
import tikitiki.util.ClassPathResourceLoader;
import tikitiki.util.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class QueryTuningHandler extends AbstractHandler {

    private static final String PATTERN = "/tikitiki/query_tuning/";

    private byte[] errorHtmlBytes;

    private CacheManager manager;

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        errorHtmlBytes = Strings.compress(ClassPathResourceLoader.load("error.html"));
        manager = CacheManager.getInstance();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        if (!target.startsWith(PATTERN)) {
            responseError(request, response);
            return;
        }

        String number = target.substring(target.lastIndexOf("/") + 1);
        byte[] responseBytes = manager.get(number);
        if (responseBytes == null) {
            responseError(request, response);
            return;
        }

        response.setContentType("text/html");
        response.setHeader("Content-Encoding", "gzip");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().write(responseBytes);
        request.setHandled(true);
    }

    private void responseError(Request request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Content-Encoding", "gzip");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().write(errorHtmlBytes);
        request.setHandled(true);
    }
}