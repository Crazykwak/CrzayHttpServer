package http.resolver;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import http.config.HttpConfig;
import http.freemarker.FreemarkerConfig;
import http.messages.HttpRequest;
import http.messages.HttpResponse;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FreemarkerResolver implements Resolver {

    private Configuration configuration;
    private HttpConfig httpConfig;
    private Template notFound;
    private static final String defaultNotFound = "./static/notFound.ftl";

    public FreemarkerResolver() {
        this(defaultNotFound);
    }

    public FreemarkerResolver(String notFound) {
        configuration = FreemarkerConfig.getConfiguration();
        this.httpConfig = HttpConfig.getInstance();
        try {
            this.notFound = configuration.getTemplate(notFound);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("notFound File is not Found. lol\n use default ftl file");
            loadDefaultFtl();
        }
    }

    private void loadDefaultFtl() {
        try {
            this.notFound = configuration.getTemplate(defaultNotFound);
        } catch (IOException e) {
            System.out.println("Default Setting is weird. check static directory. notFound.ftl");
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] handle(HttpRequest httpRequest) {

        String path = httpRequest.getPath();
        Template template = findTemplate(httpConfig.getRootPath() + path);

        ByteArrayOutputStream outPut = new ByteArrayOutputStream();
        Writer outputStreamWriter = new OutputStreamWriter(outPut);

        processTemplate(template, outputStreamWriter);
        byte[] string = outPut.toByteArray();

        return string;
    }

    private void processTemplate(Template template, Writer outputStreamWriter) {
        if (template == null) {
            template = notFound;
        }

        try {
            template.process(new HashMap<String, Object>(), outputStreamWriter);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Template findTemplate(String path) {
        if (path == null) {
            return notFound;
        }
        Template template = null;
        try {
            template = configuration.getTemplate(path);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("return 404");
            return notFound;
        }

        return template;
    }
}
