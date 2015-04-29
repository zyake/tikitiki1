package tikitiki.web;

import tikitiki.util.CacheManager;
import tikitiki.util.ClassPathResourceLoader;
import tikitiki.util.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class QueryTuningServer {

    private static final byte[] PATTERN_BYTES = "/tikitiki/query_tuning/".getBytes();

    private static final int MAX_PATH_SIZE = PATTERN_BYTES.length + 3;

    private static final int CHAR_G = (int) 'G';

    private static final int CHAR_E = (int) 'E';

    private static final int CHAR_T = (int) 'T';

    private static final int WHITE_SPACE = (int) ' ';

    private static final int MIN_NUMBER_CODE = (int) '0';

    private static final int MAX_NUMBER_CODE = (int) '9';

    private static final int FIRST_LINE_PARSE_STATE_ON_METHOD = 0;

    private static final int FIRST_LINE_PARSE_STATE_ON_REL_PATH = 1;

    private final String host;

    private final int port;

    private byte[] errorHtmlBytes;

    private volatile boolean isStarted = false;

    public QueryTuningServer(String host, int port) {
        errorHtmlBytes = createErrorResponseBytes(ClassPathResourceLoader.load("error.html"));
        this.host = host;
        this.port = port;
    }

    private byte[] createErrorResponseBytes(String errorHtml) {
        byte[] compressedErrorHtmlBytes = Strings.compress(errorHtml);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] errorResponseBytes = ("HTTP/1.1 200 OK\r\n" +
        "Content-Type: text/html; charset=UTF-8\r\n" +
        "Content-Encoding: gzip\r\n" +
        "Content-Length: " + compressedErrorHtmlBytes.length + "\r\n\r\n").getBytes();
        try {
            outputStream.write(errorResponseBytes);
            outputStream.write(compressedErrorHtmlBytes);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    // 単一リクエストのレスポンスタイムのみを追求するので、　
    // CPUをつかみっぱなしのnon-blocking I/Oですよ!
    // マルチスレッドすら使いませんが、何か?
    // RFCもガン無視ですよ!
    public void start() throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(host, port));
        System.out.println("server=" + serverSocketChannel.getLocalAddress());

        serverSocketChannel.configureBlocking(false);
        try {
            int methodCount;
            int[] method = new int[3];
            int firstLineParseState;
            int relPathBytesCount;
            int[] relPathBytes;
            InputStream inputStream;

            isStarted = true;

            while (true) {
                methodCount = 0;
                relPathBytesCount = 0;
                relPathBytes = new int[MAX_PATH_SIZE];
                firstLineParseState = FIRST_LINE_PARSE_STATE_ON_METHOD;
                byte[][] cachedResponse = CacheManager.getInstance().getCache();

                SocketChannel channel = serverSocketChannel.accept();
                if (channel == null) {
                    continue;
                }

                Socket request = channel.socket();
                inputStream = request.getInputStream();
                int data;
                OUTER:
                while ((data = inputStream.read()) != -1) {
                    if (firstLineParseState == FIRST_LINE_PARSE_STATE_ON_METHOD) {
                        method[methodCount] = data;
                        methodCount++;
                        if (methodCount == 3) {
                            if (method[0] == CHAR_G && method[1] == CHAR_E && method[2] == CHAR_T){
                                firstLineParseState++;
                                // 空白の呼び飛ばし
                                inputStream.read();
                            }else{
                                request.getOutputStream().write(errorHtmlBytes);
                                request.close();
                                break OUTER;
                            }
                        }
                    } else if (firstLineParseState == FIRST_LINE_PARSE_STATE_ON_REL_PATH) {
                        if (data == WHITE_SPACE) {
                            int numCount = relPathBytesCount - PATTERN_BYTES.length;
                            if (numCount == 0) {
                                request.getOutputStream().write(errorHtmlBytes);
                                request.close();
                                break OUTER;
                            } else {
                                int number = 0;
                                for (int i = 0 ; i < numCount; i ++) {
                                    int numberByte = relPathBytes[PATTERN_BYTES.length + i];
                                    if (MIN_NUMBER_CODE <= numberByte && numberByte <= MAX_NUMBER_CODE) {
                                        switch (numCount - i) {
                                            case 3:
                                                number += (numberByte - MIN_NUMBER_CODE) * 100;
                                            break;
                                            case 2:
                                                number += (numberByte - MIN_NUMBER_CODE) * 10;
                                            break;
                                            case 1:
                                                number += numberByte - MIN_NUMBER_CODE;
                                        }
                                    } else {
                                        request.getOutputStream().write(errorHtmlBytes);
                                        request.close();
                                        break OUTER;
                                    }
                                }
                                if (number < 1 || 100 < number) {
                                    request.getOutputStream().write(errorHtmlBytes);
                                    request.getOutputStream().flush();
                                    request.close();
                                    break OUTER;
                                }
                                byte[] outputBytes = cachedResponse[number];
                                request.getOutputStream().write(outputBytes);
                                request.getOutputStream().flush();
                                request.close();
                                break OUTER;
                            }
                        } else {
                            if (relPathBytesCount < PATTERN_BYTES.length) {
                                if (data != PATTERN_BYTES[relPathBytesCount]) {
                                    request.getOutputStream().write(errorHtmlBytes);
                                    request.close();
                                    break OUTER;
                                }
                            } else if (relPathBytesCount > MAX_PATH_SIZE) {
                                request.getOutputStream().write(errorHtmlBytes);
                                request.close();
                                break OUTER;
                            }
                            relPathBytes[relPathBytesCount++] = data;
                        }
                    }
                }
            }
        } catch (Exception e) {
            serverSocketChannel.close();
            System.err.println(e);
        }
    }
    public boolean isStarted() {
        return isStarted;
    }

    public String getURI() {
        return "http://" + host;
    }
}