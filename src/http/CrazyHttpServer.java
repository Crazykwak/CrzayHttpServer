package http;

import http.server.HttpServer;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
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

        printLog();


    }

    private static void printLog() {

        System.out.println(logo);

    }

}

