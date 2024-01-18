package http.resolver;

import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolverMaster {

    private static ResolverMaster resolverMaster;
    private Resolver defaultResolver;
    private HashMap<String, Resolver> resolverMap = new HashMap<>();

    private ResolverMaster() {
        // 기본 리졸버 생성
        Resolver freemarkerResolver = new FreemarkerResolver();
        Resolver imageResolver = new ImageResolver();
        this.defaultResolver = new DefaultResolver();

        Map<String, Object> load = null;
        try {
            load = (Map<String, Object>) new Yaml().load(new FileReader("config/servlet.yml"));
        } catch (FileNotFoundException e) {
            System.out.println("servlet.yml이 없음. 기동 중지");
            throw new RuntimeException(e);
        }

        Map<String, Object> dogMeat = (Map<String, Object>) load.get("dogMeat");
        Map<String, Object> config = (Map<String, Object>) dogMeat.get("config");
        Map<String, Object> o = (Map<String, Object>) config.get("resolver");
        List<String> freemarkerExtensionList = (List<String>) o.get("freemarkerExtension");
        for (String freemarkerExtension : freemarkerExtensionList) {
            resolverMap.put(freemarkerExtension, freemarkerResolver);
        }
        List<String> imageFileExtensionList = (List<String>) o.get("imageFileExtension");
        for (String imageFileExtension : imageFileExtensionList) {
            resolverMap.put(imageFileExtension, imageResolver);
        }
    }

    public synchronized static ResolverMaster getInstance() {
        if (resolverMaster == null) {
            resolverMaster = new ResolverMaster();
        }
        return resolverMaster;
    }

    public synchronized Resolver getResolver(String path) {
        String extension = path.substring(path.lastIndexOf(".") + 1);
        return resolverMap.getOrDefault(extension, this.defaultResolver);
    }
}
