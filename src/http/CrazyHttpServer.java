package http;

import http.server.HttpServer;
import http.test.TestUnit;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CrazyHttpServer {

    private static String logo =
            "  ___  ____    __    ____  _  _    _   _  ____  ____  ____    ___  ____  ____  _  _  ____  ____ \n" +
            " / __)(  _ \\  /__\\  (_   )( \\/ )  ( )_( )(_  _)(_  _)(  _ \\  / __)( ___)(  _ \\( \\/ )( ___)(  _ \\\n" +
            "( (__  )   / /(__)\\  / /_  \\  /    ) _ (   )(    )(   )___/  \\__ \\ )__)  )   / \\  /  )__)  )   /\n" +
            " \\___)(_)\\_)(__)(__)(____) (__)   (_) (_) (__)  (__) (__)    (___/(____)(_)\\_)  \\/  (____)(_)\\_)";


    public static void main(String[] args) {


        HttpServer httpServer = new HttpServer();
        httpServer.start();

        TestUnit[] threads = new TestUnit[200];

        try {
            for (int i = 0; i < 200; i++) {
                TestUnit test = new TestUnit("http://localhost:8080/index.ftl", "thread" + i);
                threads[i] = test;
                test.start();
            }

            for (TestUnit thread : threads) {
                thread.join();
            }

            int count = 0;
            for (TestUnit thread : threads) {
                count += thread.getSuccessResult();
            }

            System.out.println("success Count = " + count / 100);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        printLog();


    }

    private static void printLog() {

        System.out.println(logo);

    }

}

