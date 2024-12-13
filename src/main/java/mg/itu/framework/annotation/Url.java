package mg.itu.framework.model;

import mg.itu.framework.annotation.HttpMethod;

/**
 * SPRINT 2 : Classe pour stocker une URL et sa méthode HTTP
 * 
 * @version Sprint 2 - 11 octobre 2024
 */
public class Url {
    private final String urlpattern;
    private final HttpMethod method;
    
    public Url(String url, HttpMethod method) {
        this.urlpattern = url;
        this.method = method;
    }
    
    public String getUrlpattern() {
        return urlpattern;
    }
    
    public HttpMethod getMethod() {
        return method;
    }
}