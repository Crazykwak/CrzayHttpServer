package http.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TestUnit extends Thread {

    private final URL url;
    private final String threadName;
    private int count = 0;
    private int err = 0;

    public TestUnit(String url, String threadName) throws MalformedURLException {
        this.url = new URL(url);
        this.threadName = threadName;
    }

    @Override
    public void run() {

        InputStream inputStream = null;
        BufferedReader reader = null;
        try {

            for (int i = 0; i < 100; i++) {
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(3000);

                urlConnection.connect();
                Object content = urlConnection.getContent();

                if (content instanceof InputStream) {
                    // 컨텐트를 읽기 위해 InputStream을 사용
                    inputStream = (InputStream) content;
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("success")) count++;
                    }

                    reader.close();
                    inputStream.close();

                } else {
                    err++;
                    System.out.println("컨텐트를 읽을 수 없습니다.");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getSuccessResult() {
        return count;
    }
}
