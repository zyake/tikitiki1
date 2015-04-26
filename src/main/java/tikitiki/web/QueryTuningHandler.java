package tikitiki.web;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import tikitiki.util.CacheManager;
import tikitiki.util.ClassPathResourceLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class QueryTuningHandler extends AbstractHandler {

    private static final Pattern PATTERN = Pattern.compile("^/tikitiki/query_tuning/\\d+$");

    private byte[] css;

    private byte[] errorHtml;

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        css = ClassPathResourceLoader.load("style.css").getBytes();
        errorHtml = ClassPathResourceLoader.load("error.html").getBytes();
    }

    @Override
    public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        if (!PATTERN.matcher(target).find()) {
            if (target.equals("/lib/css/style.css")) {
                responseCSS(request, response);
                return;
            }
            responseError(request, response);
            return;
        }

        String number = target.substring(target.lastIndexOf("/") + 1);
        int requestPattern = Integer.parseInt(number);
        if (requestPattern < 1 || 100 < requestPattern) {
            responseError(request, response);
            return;
        }

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        byte[] cacheValue = CacheManager.getInstance().get(number);
        response.getOutputStream().write(cacheValue);
        request.setHandled(true);
    }

    private void responseCSS(Request request, HttpServletResponse response) throws IOException {
        response.setContentType("text/css");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().write(css);
        request.setHandled(true);
    }

    private void responseError(Request request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().write(errorHtml);
        request.setHandled(true);
    }
}
