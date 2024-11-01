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
import java.util.Map;

/**
 * SPRINT 3 : Support de ModelView
 * 
 * Le servlet peut maintenant traiter les retours ModelView
 * et faire un forward vers la JSP correspondante.
 * 
 * @version Sprint 3 - 1 novembre 2024
 */
public class UrlListener extends HttpServlet {
    
    private HashMap<String, Url> urlMappings;
    
    @Override
    public void init() throws ServletException {
        try {
            AnotationReader reader = new AnotationReader();
            urlMappings = reader.getClassesWithAnnotation(getServletContext());
            
            System.out.println("=== Sprint 3 : URLs mappées ===");
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
        
        // Chercher le mapping
        Url urlMapping = urlMappings.get(path);
        
        if (urlMapping == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = res.getWriter();
            out.println("<h3>404 Not Found</h3>");
            out.println("<p>Aucune correspondance pour : " + path + "</p>");
            out.println("<p><em>Sprint 3 - 1 novembre 2024</em></p>");
            return;
        }
        
        try {
            // Charger la classe et invoquer la méthode
            Class<?> clazz = Class.forName(urlMapping.getClassName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod(urlMapping.getMethodName());
            
            Object result = method.invoke(instance);
            
            // Sprint 3 : Traiter le retour
            if (result instanceof ModelView) {
                // Retour ModelView : forward vers JSP
                ModelView mv = (ModelView) result;
                
                // Ajouter les données en attributs de requête
                for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
                
                // Forward vers la JSP
                String jspPath = "/WEB-INF/views/" + mv.getJspName() + ".jsp";
                req.getRequestDispatcher(jspPath).forward(req, res);
                
            } else if (result instanceof String) {
                // Retour String : affichage direct (comme Sprint 2)
                PrintWriter out = res.getWriter();
                out.println((String) result);
            }
            
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'invocation de la méthode", e);
        }
    }
}