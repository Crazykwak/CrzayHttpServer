package http.config;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class DefaultServletConfig implements ServletConfig {

    private String servletName;
    private ServletContext servletContext;

    public DefaultServletConfig(String servletName, ServletContext servletContext) {
        this.servletName = servletName;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return this.servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * never use this method
     * @param s
     * @return
     */
    @Override
    public String getInitParameter(String s) {
        return null;
    }

    /**
     * never use this method
     * @return
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }
}
