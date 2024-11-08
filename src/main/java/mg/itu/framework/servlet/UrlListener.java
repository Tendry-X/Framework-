package mg.itu.framework.servlet;

import mg.itu.framework.annotation.MyAnnotation;
import mg.itu.framework.model.ModelView;
import mg.itu.framework.model.Url;
import mg.itu.framework.util.AnotationReader;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SPRINT 3bis : Support des paramètres dynamiques
 * 
 * Permet de capturer les valeurs des patterns comme /user/{id}
 * 
 * @version Sprint 3bis - 8 novembre 2024
 */
public class UrlListener extends HttpServlet {
    
    private HashMap<String, Url> urlMappings;
    private AnotationReader reader;
    
    @Override
    public void init() throws ServletException {
        try {
            reader = new AnotationReader();
            urlMappings = reader.getClassesWithAnnotation(getServletContext());
            
            System.out.println("=== Sprint 3bis : URLs mappées ===");
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
        
        // Sprint 3bis : Chercher avec support des patterns
        Map.Entry<String, List<String>> matchResult = reader.findMatchingUrl(path, urlMappings);
        
        if (matchResult == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = res.getWriter();
            out.println("<h3>404 Not Found</h3>");
            out.println("<p>Aucune correspondance pour : " + path + "</p>");
            out.println("<p><em>Sprint 3bis - 8 novembre 2024</em></p>");
            return;
        }
        
        String pattern = matchResult.getKey();
        List<String> paramValues = matchResult.getValue();
        Url urlMapping = urlMappings.get(pattern);
        
        try {
            // Sprint 3bis : Extraire les noms des paramètres
            List<String> paramNames = reader.extractParamNames(pattern);
            
            // Stocker les paramètres d'URL en attributs de requête
            for (int i = 0; i < paramNames.size() && i < paramValues.size(); i++) {
                req.setAttribute(paramNames.get(i), paramValues.get(i));
            }
            
            // Charger et invoquer
            Class<?> clazz = Class.forName(urlMapping.getClassName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod(urlMapping.getMethodName());
            
            Object result = method.invoke(instance);
            
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
}