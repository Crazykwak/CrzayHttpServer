package http.config;

import http.exception.NotFoundFreemarkerExtensionConfigException;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpConfig {

    private static HttpConfig httpConfig;
    private String rootPath;
    private List<String> freemarkerExtensionList;
    private long keepAliveTimeOut = 1000L;

    private HttpConfig(String rootPath) {
        this.rootPath = rootPath;
        loadConfig();
    }

    private void loadConfig() {
        try {
            Map<String, Object> load = (Map<String, Object>) new Yaml().load(new FileReader("config/crazyHttpConfig.yml"));
            Map<String, Object> dogMeat = getConfigSection(load, "dogMeat");
            Map<String, Object> config = getConfigSection(dogMeat, "config");
            Map<String, Object> resolver = getConfigSection(config, "resolver");
            this.freemarkerExtensionList = getConfigList(resolver, "freemarkerExtension");
        } catch (FileNotFoundException e) {
            handleConfigError("crazyHttpConfig.yml is not found. use default config");
            defaultFreemarkerExtensionSetting();
        } catch (NotFoundFreemarkerExtensionConfigException e) {
            handleConfigError("Not found FreemarkerExtension Setting. use default config");
            defaultFreemarkerExtensionSetting();
        }
    }

    private Map<String, Object> getConfigSection(Map<String, Object> config, String sectionName) throws NotFoundFreemarkerExtensionConfigException {
        if (config == null || !config.containsKey(sectionName)) {
            throw new NotFoundFreemarkerExtensionConfigException();
        }
        return (Map<String, Object>) config.get(sectionName);
    }

    private List<String> getConfigList(Map<String, Object> config, String listName) throws NotFoundFreemarkerExtensionConfigException {
        Map<String, Object> resolver = getConfigSection(config, listName);
        if (resolver == null || !resolver.containsKey(listName)) {
            throw new NotFoundFreemarkerExtensionConfigException();
        }
        return (List<String>) resolver.get(listName);
    }

    private void handleConfigError(String errorMessage) {
        System.out.println(errorMessage);
    }

    private void defaultFreemarkerExtensionSetting() {
        freemarkerExtensionList = new ArrayList<>();
        freemarkerExtensionList.add("ftl");
        freemarkerExtensionList.add("api");
        freemarkerExtensionList.add("fsp");
    }

    public String getRootPath() {
        return rootPath;
    }

    public List<String> getFreemarkerExtensionList() {
        return freemarkerExtensionList;
    }

    public synchronized static HttpConfig getInstance() {
        if (httpConfig == null) {
            httpConfig = new HttpConfig("./webapp");
        }
        return httpConfig;
    }

    public long getKeepAliveTimeOut() {
        return keepAliveTimeOut;
    }
}
