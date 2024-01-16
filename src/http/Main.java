package http;

import http.server.HttpServer;
import http.servlet.DefaultServlet;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Main {


    public static void main(String[] args) {

        String servletClassName = null;
        String servletName = null;
        Map<String, String> servletContext = null;

        try {
            Map<String, Object> load = (Map<String, Object>) new Yaml().load(new FileReader("config/servlet.yml"));
            System.out.println("load = " + load.toString());
            Map<String, Object> dogMeat = (Map<String, Object>) load.get("dogMeat");
            Map<String, Object> config = (Map<String, Object>) dogMeat.get("config");
            Map<String, Object> servlet = (Map<String, Object>) config.get("servlet");

            servletClassName = (String) servlet.getOrDefault("class", "hihi");
            servletName = (String) servlet.getOrDefault("name", "basicServlet");
            servletContext = (Map<String, String>) servlet.getOrDefault("context", new HashMap<>());


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Servlet servletClass;
        try {
            Class<?> loadClass = Class.forName(servletClassName);
            servletClass = (Servlet) loadClass.newInstance();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        System.out.println(servletClass.getClass().getName());
        System.out.println("servletClassName = " + servletClassName);
        System.out.println("servletName = " + servletName);
        for (Map.Entry<String, String> stringStringEntry : servletContext.entrySet()) {
            System.out.println("stringStringEntry = " + stringStringEntry.getKey() + " : " + stringStringEntry.getValue());
        }

        HttpServer httpServer = new HttpServer();
        httpServer.start();


    }

}

