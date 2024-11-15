package mg.itu.framework.util;

import mg.itu.framework.annotation.MyAnnotation;
import mg.itu.framework.model.Url;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SPRINT 3bis : Support des paramètres dynamiques dans l'URL
 * 
 * Permet de mapper des URLs avec patterns comme /user/{id}
 * 
 * @version Sprint 3bis - 8 novembre 2024
 */
public class AnotationReader {
    
    public HashMap<String, Url> getClassesWithAnnotation(ServletContext context) throws Exception {
        HashMap<String, Url> urlMap = new HashMap<>();
        
        InputStream is = context.getResourceAsStream("/WEB-INF/web.xml");
        PackageReader packageReader = new PackageReader();
        String packageName = packageReader.read(is, "package");
        
        if (packageName == null || packageName.isEmpty()) {
            throw new Exception("Aucun package défini dans web.xml");
        }
        
        List<Class<?>> classes = packageReader.getClasses(packageName);
        
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(MyAnnotation.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(MyAnnotation.class)) {
                        MyAnnotation annotation = method.getAnnotation(MyAnnotation.class);
                        String urlPattern = annotation.value();
                        
                        Url url = new Url(urlPattern, annotation.method());
                        url.setClassName(clazz.getName());
                        url.setMethodName(method.getName());
                        
                        urlMap.put(urlPattern, url);
                    }
                }
            }
        }
        
        return urlMap;
    }
    
    /**
     * SPRINT 3bis : Trouver le mapping correspondant avec support des patterns
     * 
     * Exemple : /user/{id} matche /user/123
     */
    public Map.Entry<String, List<String>> findMatchingUrl(String requestPath, HashMap<String, Url> urlMappings) {
        // D'abord chercher une correspondance exacte
        if (urlMappings.containsKey(requestPath)) {
            return new HashMap.SimpleEntry<>(requestPath, new ArrayList<>());
        }
        
        // Sprint 3bis : Chercher avec patterns
        for (Map.Entry<String, Url> entry : urlMappings.entrySet()) {
            String pattern = entry.getKey();
            
            // Vérifier si le pattern contient des paramètres {xxx}
            if (pattern.contains("{")) {
                // Convertir /user/{id} en regex /user/([^/]+)
                String regex = pattern.replaceAll("\\{[^}]+\\}", "([^/]+)");
                regex = "^" + regex + "$";
                
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(requestPath);
                
                if (m.matches()) {
                    // Extraire les valeurs des paramètres
                    List<String> paramValues = new ArrayList<>();
                    for (int i = 1; i <= m.groupCount(); i++) {
                        paramValues.add(m.group(i));
                    }
                    
                    return new HashMap.SimpleEntry<>(pattern, paramValues);
                }
            }
        }
        
        return null;
    }
    
    /**
     * SPRINT 3bis : Extraire les noms des paramètres du pattern
     * 
     * Exemple : /user/{id}/post/{postId} -> ["id", "postId"]
     */
    public List<String> extractParamNames(String pattern) {
        List<String> paramNames = new ArrayList<>();
        Pattern p = Pattern.compile("\\{([^}]+)\\}");
        Matcher m = p.matcher(pattern);
        
        while (m.find()) {
            paramNames.add(m.group(1));
        }
        
        return paramNames;
    }
}