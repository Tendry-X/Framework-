package mg.itu.framework.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * SPRINT 1 : Servlet centrale qui intercepte toutes les URLs
 * 
 * Cette servlet affiche l'URL demandée par l'utilisateur.
 * C'est le point d'entrée de toutes les requêtes HTTP.
 * 
 * Exemple d'utilisation :
 * - L'utilisateur tape : http://localhost:8080/app/hello
 * - La page affiche : "URL demandée : /hello"
 * 
 * @author Framework MVC Team
 * @version 1.0 - Sprint 1
 */
public class UrlListener extends HttpServlet {

    /**
     * Méthode appelée pour TOUTES les requêtes HTTP (GET, POST, etc.)
     * 
     * @param req  La requête HTTP
     * @param res  La réponse HTTP
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        
        // Récupérer l'URL complète
        String uri = req.getRequestURI();
        
        // Récupérer le context path (ex: /framework-test)
        String contextPath = req.getContextPath();
        
        // Extraire l'URL relative (sans le context path)
        String path = uri.substring(contextPath.length());
        
        // Préparer la réponse HTML
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        
        // Afficher une page HTML avec l'URL
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>Framework MVC - Sprint 1</title>");
        out.println("    <style>");
        out.println("        body { font-family: Arial, sans-serif; margin: 50px; }");
        out.println("        h1 { color: #2c3e50; }");
        out.println("        .info { background: #ecf0f1; padding: 20px; border-radius: 5px; }");
        out.println("        .path { color: #e74c3c; font-weight: bold; font-size: 1.2em; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <h1>✅ Framework MVC - Sprint 1</h1>");
        out.println("    <div class='info'>");
        out.println("        <h2>URL interceptée avec succès !</h2>");
        out.println("        <p><strong>URL demandée :</strong> <span class='path'>" + path + "</span></p>");
        out.println("        <hr>");
        out.println("        <p><strong>URI complet :</strong> " + uri + "</p>");
        out.println("        <p><strong>Context Path :</strong> " + contextPath + "</p>");
        out.println("        <p><strong>Méthode HTTP :</strong> " + req.getMethod() + "</p>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
        
        out.flush();
    }
}