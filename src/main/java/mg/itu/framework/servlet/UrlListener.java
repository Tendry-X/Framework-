package mg.itu.framework.servlet;

import mg.itu.framework.annotation.HttpMethod;
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
import java.util.*;

/**
 * SPRINT 8 : Gestion des formulaires et données complexes
 * 
 * Nouvelles fonctionnalités :
 * - Récupération automatique de tous les paramètres dans une Map
 * - Support des tableaux de valeurs (checkbox multiples, etc.)
 * - Gestion des données de formulaires complexes
 * 
 * @version Sprint 8 - 13 décembre 2024
 */
public class UrlListener extends HttpServlet {
    
    private HashMap<String, Url> urlMappings;
    private AnotationReader reader;
    
    @Override
    public void init() throws ServletException {
        try {
            reader = new AnotationReader();
            urlMappings = reader.getClassesWithAnnotation(getServletContext());
            
            System.out.println("=== Sprint 8 : URLs mappées ===");
            for (Map.Entry<String, Url> entry : urlMappings.entrySet()) {
                Url url = entry.getValue();
                System.out.println("URL: " + entry.getKey() + " [" + url.getMethod() + "] -> " + 
                                 url.getClassName() + "." + url.getMethodName());
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
        String httpMethod = req.getMethod();
        
        res.setContentType("text/html;charset=UTF-8");
        
        // Chercher avec support des patterns
        Map.Entry<String, List<String>> matchResult = reader.findMatchingUrl(path, urlMappings);
        
        if (matchResult == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = res.getWriter();
            out.println("<h3>404 Not Found</h3>");
            out.println("<p>Aucune correspondance pour : " + path + "</p>");
            out.println("<p><em>Sprint 8 - 13 décembre 2024</em></p>");
            return;
        }
        
        String pattern = matchResult.getKey();
        List<String> paramValues = matchResult.getValue();
        Url urlMapping = urlMappings.get(pattern);
        
        // Vérifier que la méthode HTTP correspond
        if (!httpMethod.equalsIgnoreCase(urlMapping.getMethod().name())) {
            res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            PrintWriter out = res.getWriter();
            out.println("<h3>405 Method Not Allowed</h3>");
            out.println("<p>Méthode HTTP non autorisée pour : " + path + "</p>");
            out.println("<p>Attendu : " + urlMapping.getMethod() + ", Reçu : " + httpMethod + "</p>");
            out.println("<p><em>Sprint 8 - 13 décembre 2024</em></p>");
            return;
        }
        
        try {
            // Extraire les noms des paramètres d'URL
            List<String> urlParamNames = reader.extractParamNames(pattern);
            
            // Créer une Map des paramètres d'URL
            Map<String, String> urlParams = new HashMap<>();
            for (int i = 0; i < urlParamNames.size() && i < paramValues.size(); i++) {
                urlParams.put(urlParamNames.get(i), paramValues.get(i));
            }
            
            // SPRINT 8 : Créer une Map de TOUS les paramètres de la requête
            Map<String, Object> allParameters = new HashMap<>();
            Enumeration<String> parameterNames = req.getParameterNames();
            
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String[] values = req.getParameterValues(paramName);
                
                if (values.length == 1) {
                    // Un seul paramètre : stocker la String
                    allParameters.put(paramName, values[0]);
                } else {
                    // Plusieurs valeurs : stocker le tableau
                    allParameters.put(paramName, values);
                }
            }
            
            // Ajouter aussi les paramètres d'URL
            allParameters.putAll(urlParams);
            
            // Charger la classe
            Class<?> clazz = Class.forName(urlMapping.getClassName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod(urlMapping.getMethodName());
            
            // Préparer les arguments de la méthode
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                Class<?> paramType = param.getType();
                
                // SPRINT 8 : Si le paramètre est une Map<String, Object>, injecter tous les paramètres
                if (paramType == Map.class || paramType == HashMap.class) {
                    args[i] = allParameters;
                    continue;
                }
                
                // SPRINT 8 : Si le paramètre est HttpServletRequest, l'injecter
                if (paramType == HttpServletRequest.class) {
                    args[i] = req;
                    continue;
                }
                
                // SPRINT 8 : Si le paramètre est HttpServletResponse, l'injecter
                if (paramType == HttpServletResponse.class) {
                    args[i] = res;
                    continue;
                }
                
                // Déterminer le nom du paramètre
                String paramName;
                if (param.isAnnotationPresent(MyParam.class)) {
                    MyParam myParam = param.getAnnotation(MyParam.class);
                    paramName = myParam.value();
                } else {
                    paramName = param.getName();
                }
                
                String paramValue = null;
                
                // ORDRE DE PRIORITÉ
                // 1. Paramètres d'URL {id}
                if (urlParams.containsKey(paramName)) {
                    paramValue = urlParams.get(paramName);
                }
                
                // 2. Paramètres de requête
                if (paramValue == null) {
                    paramValue = req.getParameter(paramName);
                }
                
                // 3. Valeur par défaut
                if (paramValue != null) {
                    args[i] = Transtipation.convert(paramValue, paramType);
                } else {
                    args[i] = getDefaultValue(paramType);
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