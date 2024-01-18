package http.freemarker;


import freemarker.template.Configuration;

public class FreemarkerConfig {

    private static Configuration configuration;

    private FreemarkerConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public synchronized static Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
        }

        return configuration;
    }
}
