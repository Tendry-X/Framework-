package mg.itu.framework.util;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * SPRINT 2bis : Scanner tous les packages récursivement
 * 
 * @version Sprint 2bis - 25 octobre 2024
 */
public class PackageReader {
    
    public String read(InputStream is, String conf) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            NodeList confList = doc.getElementsByTagName(conf);
            if (confList.getLength() > 0) {
                return confList.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Sprint 2bis : Scan récursif de tous les sous-packages
     */
    public List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            
            if ("file".equals(protocol)) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    // Sprint 2bis : Scan récursif des sous-dossiers
                    classes.addAll(findClassesRecursive(directory, packageName));
                }
            } else if ("jar".equals(protocol)) {
                String urlPath = resource.getPath();
                String jarPath = urlPath.substring(5, urlPath.indexOf("!"));
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(
                        java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
                    classes.addAll(findClassesInJar(jar, packageName.replace('.', '/')));
                }
            }
        }
        return classes;
    }
    
    private List<Class<?>> findClassesInJar(java.util.jar.JarFile jarFile, String path) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
        
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                String className = name.replace("/", ".").replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }
    
    /**
     * Sprint 2bis : Méthode récursive pour scanner tous les sous-dossiers
     */
    private List<Class<?>> findClassesRecursive(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        
        if (!directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Sprint 2bis : Récursion dans les sous-packages
                    classes.addAll(findClassesRecursive(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().replace(".class", "");
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        System.err.println("Classe non trouvée : " + className);
                    }
                }
            }
        }
        return classes;
    }
}