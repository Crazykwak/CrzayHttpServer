package http.resolver;

import http.config.HttpConfig;

import java.util.HashMap;

public class ResolverMaster {

    private static ResolverMaster resolverMaster;
    private Resolver defaultResolver;
    private HashMap<String, Resolver> resolverMap = new HashMap<>();
    private final HttpConfig httpConfig;

    private ResolverMaster() {
        // 기본 리졸버 생성
        Resolver freemarkerResolver = new FreemarkerResolver();
        this.defaultResolver = new DefaultResolver();
        this.httpConfig = HttpConfig.getInstance();
        for (String freemarkerExtension : httpConfig.getFreemarkerExtensionList()) {
            resolverMap.put(freemarkerExtension, freemarkerResolver);
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
