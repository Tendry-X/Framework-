package mg.itu.framework.util;

import mg.itu.framework.annotation.MyAnnotation;
import mg.itu.framework.model.Url;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import javax.servlet.ServletContext;

public class AnotationReader {

    public Map<Url, Map<Class<?>, Method>> getClassesWithAnnotation(ServletContext context) throws Exception {
        Map<Url, Map<Class<?>, Method>> annotatedMap = new HashMap<>();
        PackageReader packageReader = new PackageReader();

        try (InputStream is = context.getResourceAsStream("/resources/config.xml")) {
            if (is == null) {
                throw new RuntimeException("config.xml introuvable");
            }

            String packagePath = packageReader.read(is, "package");
            List<Class<?>> classes = packageReader.getClasses(packagePath);

            for (Class<?> c : classes) {
                String classPath = "";

                if (c.isAnnotationPresent(MyAnnotation.class)) {
                    classPath = c.getAnnotation(MyAnnotation.class).value();
                }

                for (Method m : c.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(MyAnnotation.class)) {
                        String methodPath = m.getAnnotation(MyAnnotation.class).value();
                        String fullPath = (classPath + methodPath).replaceAll("//+", "/");
                        Url u = new Url(convertToRegex(fullPath), m.getAnnotation(MyAnnotation.class).method());
                        Map<Class<?>, Method> classMethodMap = new HashMap<>();
                        classMethodMap.put(c, m);
                        annotatedMap.put(u, classMethodMap);
                    }
                }
            }
        }

        return annotatedMap;
    }

    public static Map<Class<?>, Method> getMatches(String path, Map<Url, Map<Class<?>, Method>> classes, String Methods)
            throws Exception {
        Map<Class<?>, Method> match = null;

        for (Url mA : classes.keySet()) {
            String pattern = mA.getUrlpattern();
            if (path.matches(pattern) && mA.getMethod().toString().equals(Methods)) {
                match = classes.get(mA);
                break;
            }
        }
        return match;
    }

    private String convertToRegex(String path) {
        String regex = path.replaceAll("\\{[^/]+\\}", "([^/]+)");
        return "^" + regex + "$";
    }

    public static Map.Entry<Map<Class<?>, Method>, List<String>> getMatchesWithParams(String path,
            Map<Url, Map<Class<?>, Method>> classes, String httpMethod) {
        for (Url url : classes.keySet()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(url.getUrlpattern()).matcher(path);
            if (matcher.matches() && url.getMethod().toString().equalsIgnoreCase(httpMethod)) {
                List<String> params = new ArrayList<>();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    params.add(matcher.group(i));
                }
                return new AbstractMap.SimpleEntry<>(classes.get(url), params);
            }
        }
        return null;
    }
}
