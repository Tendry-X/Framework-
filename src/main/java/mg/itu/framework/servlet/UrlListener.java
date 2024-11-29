package mg.itu.framework.servlet;

import mg.itu.framework.annotation.MyAnnotation;
import mg.itu.framework.annotation.MyParam;
import mg.itu.framework.model.ModelView;
import mg.itu.framework.model.Url;
import mg.itu.framework.util.AnotationReader;
import mg.itu.framework.util.Transtipation;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SPRINT 6ter : Injection avec ordre de priorité
 * 
 * Ordre de recherche des paramètres :
 * 1. Paramètres d'URL (exemple: /user/{id})
 * 2. Paramètres de requête (request.getParameter())
 * 3. Valeur par défaut (null ou 0 selon le type)
 * 
 * @version Sprint 6ter - 29 novembre 2024
 */
public class UrlListener extends HttpServlet {
    
    private HashMap<String, Url> urlMappings;
    private AnotationReader reader;
    
    @Override
    public void init() throws ServletException {
        try {
            reader = new AnotationReader();
            urlMappings = reader.getClassesWithAnnotation(getServletContext());
            
            System.out.println("=== Sprint 6ter : URLs mappées ===");
            for (Map.Entry<String, Url> entry : urlMappings.entrySet()) {
                System.out.println("URL: " + entry.getKey() + " -> " + 
                                 entry.getValue().getClassName() + "." + 
                                 entry.getValue().getMethodName());
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des annotations", e);
        }
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());
        
        res.setContentType("text/html;charset=UTF-8");
        
        // Chercher avec support des patterns
        Map.Entry<String, List<String>> matchResult = reader.findMatchingUrl(path, urlMappings);
        
        if (matchResult == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = res.getWriter();
            out.println("<h3>404 Not Found</h3>");
            out.println("<p>Aucune correspondance pour : " + path + "</p>");
            out.println("<p><em>Sprint 6ter - 29 novembre 2024</em></p>");
            return;
        }
        
        String pattern = matchResult.getKey();
        List<String> paramValues = matchResult.getValue();
        Url urlMapping = urlMappings.get(pattern);
        
        try {
            // Extraire les noms des paramètres d'URL
            List<String> urlParamNames = reader.extractParamNames(pattern);
            
            // SPRINT 6ter : Créer une Map des paramètres d'URL
            Map<String, String> urlParams = new HashMap<>();
            for (int i = 0; i < urlParamNames.size() && i < paramValues.size(); i++) {
                urlParams.put(urlParamNames.get(i), paramValues.get(i));
            }
            
            // Charger la classe
            Class<?> clazz = Class.forName(urlMapping.getClassName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod(urlMapping.getMethodName());
            
            // Préparer les arguments de la méthode
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                String paramName;
                
                // Déterminer le nom du paramètre
                if (param.isAnnotationPresent(MyParam.class)) {
                    MyParam myParam = param.getAnnotation(MyParam.class);
                    paramName = myParam.value();
                } else {
                    paramName = param.getName();
                }
                
                String paramValue = null;
                
                // SPRINT 6ter : ORDRE DE PRIORITÉ
                
                // 1. Priorité 1 : Chercher dans les paramètres d'URL {id}
                if (urlParams.containsKey(paramName)) {
                    paramValue = urlParams.get(paramName);
                    System.out.println("Sprint 6ter - Paramètre '" + paramName + "' trouvé dans URL: " + paramValue);
                }
                
                // 2. Priorité 2 : Chercher dans request.getParameter()
                if (paramValue == null) {
                    paramValue = req.getParameter(paramName);
                    if (paramValue != null) {
                        System.out.println("Sprint 6ter - Paramètre '" + paramName + "' trouvé dans request: " + paramValue);
                    }
                }
                
                // 3. Priorité 3 : Valeur par défaut
                if (paramValue != null) {
                    // Convertir vers le type approprié
                    args[i] = Transtipation.convert(paramValue, param.getType());
                } else {
                    System.out.println("Sprint 6ter - Paramètre '" + paramName + "' non trouvé, utilisation valeur par défaut");
                    args[i] = getDefaultValue(param.getType());
                }
            }
            
            // Invoquer avec les arguments
            Object result = method.invoke(instance, args);
            
            // Traiter le retour
            if (result instanceof ModelView) {
                ModelView mv = (ModelView) result;
                
                // Ajouter les données en attributs
                for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
                
                // Forward vers JSP
                String jspPath = "/WEB-INF/views/" + mv.getJspName() + ".jsp";
                req.getRequestDispatcher(jspPath).forward(req, res);
                
            } else if (result instanceof String) {
                PrintWriter out = res.getWriter();
                out.println((String) result);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Erreur lors de l'invocation", e);
        }
    }
    
    /**
     * Valeurs par défaut selon le type
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == long.class) return 0L;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == char.class) return '\0';
        return null;
    }
}