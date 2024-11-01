package mg.itu.framework.util;

import mg.itu.framework.annotation.MyAnnotation;
import mg.itu.framework.model.Url;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SPRINT 2 : Scanner d'annotations
 * 
 * Scanne les classes avec @MyAnnotation et les méthodes annotées
 * 
 * @version Sprint 2 - 11 octobre 2024
 */
public class AnotationReader {
    
    public Map<Url, Map<Class<?>, Method>> getClassesWithAnnotation(ServletContext context) throws Exception {
        Map<Url, Map<Class<?>, Method>> annotatedMap = new HashMap<>();
        
        // Lire le package depuis web.xml
        PackageReader packageReader = new PackageReader();
        InputStream is = context.getResourceAsStream("/WEB-INF/web.xml");
        String packageName = packageReader.read(is, "param-value");
        
        if (packageName == null || packageName.isEmpty()) {
            return annotatedMap;
        }
        
        // Scanner les classes
        List<Class<?>> classes = packageReader.getClasses(packageName);
        
        for (Class<?> c : classes) {
            String classPath = "";
            
            // Si la classe a @MyAnnotation
            if (c.isAnnotationPresent(MyAnnotation.class)) {
                classPath = c.getAnnotation(MyAnnotation.class).value();
            }
            
            // Scanner les méthodes
            for (Method m : c.getDeclaredMethods()) {
                if (m.isAnnotationPresent(MyAnnotation.class)) {
                    String methodPath = m.getAnnotation(MyAnnotation.class).value();
                    String fullPath = classPath + methodPath;
                    
                    Url u = new Url(fullPath, m.getAnnotation(MyAnnotation.class).method());
                    
                    Map<Class<?>, Method> methodMap = annotatedMap.getOrDefault(u, new HashMap<>());
                    methodMap.put(c, m);
                    annotatedMap.put(u, methodMap);
                }
            }
        }
        
        return annotatedMap;
    }
    
    public static Map<Class<?>, Method> getMatches(String path, Map<Url, Map<Class<?>, Method>> classes, String httpMethod) {
        for (Url mA : classes.keySet()) {
            if (path.equals(mA.getUrlpattern()) && mA.getMethod().name().equals(httpMethod)) {
                return classes.get(mA);
            }
        }
        return null;
    }
}