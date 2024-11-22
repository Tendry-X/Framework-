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
 * SPRINT 6bis : Support de l'annotation @MyParam
 * 
 * Permet de spécifier explicitement le nom du paramètre à récupérer.
 * Exemple : void test(@MyParam("nbr") int nombre)
 * 
 * @version Sprint 6bis - 22 novembre 2024
 */
public class UrlListener extends HttpServlet {
    
    private HashMap<String, Url> urlMappings;
    private AnotationReader reader;
    
    @Override
    public void init() throws ServletException {
        try {
            reader = new AnotationReader();
            urlMappings = reader.getClassesWithAnnotation(getServletContext());
            
            System.out.println("=== Sprint 6bis : URLs mappées ===");
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
            out.println("<p><em>Sprint 6bis - 22 novembre 2024</em></p>");
            return;
        }
        
        String pattern = matchResult.getKey();
        List<String> paramValues = matchResult.getValue();
        Url urlMapping = urlMappings.get(pattern);
        
        try {
            // Extraire les noms des paramètres d'URL
            List<String> paramNames = reader.extractParamNames(pattern);
            
            // Stocker les paramètres d'URL en attributs
            for (int i = 0; i < paramNames.size() && i < paramValues.size(); i++) {
                req.setAttribute(paramNames.get(i), paramValues.get(i));
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
                
                // SPRINT 6bis : Vérifier si le paramètre a l'annotation @MyParam
                if (param.isAnnotationPresent(MyParam.class)) {
                    MyParam myParam = param.getAnnotation(MyParam.class);
                    paramName = myParam.value(); // Utiliser le nom spécifié dans @MyParam
                } else {
                    paramName = param.getName(); // Utiliser le nom par défaut (arg0, arg1...)
                }
                
                // Chercher dans request.getParameter()
                String paramValue = req.getParameter(paramName);
                
                if (paramValue != null) {
                    // Convertir vers le type approprié
                    args[i] = Transtipation.convert(paramValue, param.getType());
                } else {
                    // Valeur par défaut selon le type
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