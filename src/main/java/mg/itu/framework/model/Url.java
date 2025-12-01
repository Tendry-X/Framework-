package mg.itu.framework.model;

import mg.itu.framework.annotation.HttpMethod;

public class Url {
    private final String urlpattern;
    private final HttpMethod method;
    public Url(String url,HttpMethod method){
        this.method =  method;
        this.urlpattern = url;
    }
    public HttpMethod getMethod() {
        return method;
    }
    public String getUrlpattern() {
        return urlpattern;
    }
}