package mg.itu.framework.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * SPRINT 1 : Affichage de l'URL demandée
 * 
 * Servlet qui intercepte toutes les requêtes et affiche l'URL demandée.
 * 
 * @version Sprint 1 - 4 octobre 2024
 */
public class UrlListener extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        
        // Récupérer l'URL demandée
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());
        
        // Afficher la page HTML
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>Framework MVC - Sprint 1</title>");
        out.println("    <style>");
        out.println("        body {");
        out.println("            font-family: 'Segoe UI', Arial, sans-serif;");
        out.println("            margin: 50px;");
        out.println("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");
        out.println("            min-height: 100vh;");
        out.println("        }");
        out.println("        .container {");
        out.println("            background: white;");
        out.println("            padding: 40px;");
        out.println("            border-radius: 15px;");
        out.println("            box-shadow: 0 10px 30px rgba(0,0,0,0.3);");
        out.println("            max-width: 600px;");
        out.println("            margin: 0 auto;");
        out.println("        }");
        out.println("        h1 {");
        out.println("            color: #667eea;");
        out.println("            margin-top: 0;");
        out.println("        }");
        out.println("        .info {");
        out.println("            background: #f8f9fa;");
        out.println("            padding: 20px;");
        out.println("            border-radius: 8px;");
        out.println("            border-left: 4px solid #667eea;");
        out.println("        }");
        out.println("        .path {");
        out.println("            color: #e74c3c;");
        out.println("            font-weight: bold;");
        out.println("            font-size: 1.3em;");
        out.println("            font-family: 'Courier New', monospace;");
        out.println("        }");
        out.println("        .badge {");
        out.println("            display: inline-block;");
        out.println("            background: #667eea;");
        out.println("            color: white;");
        out.println("            padding: 5px 15px;");
        out.println("            border-radius: 20px;");
        out.println("            font-size: 0.85em;");
        out.println("            margin-top: 10px;");
        out.println("        }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='container'>");
        out.println("        <h1>✅ Framework MVC - Sprint 1</h1>");
        out.println("        <div class='info'>");
        out.println("            <h2>URL interceptée avec succès !</h2>");
        out.println("            <p><strong>URL demandée :</strong></p>");
        out.println("            <p class='path'>" + path + "</p>");
        out.println("            <hr>");
        out.println("            <p><strong>URI complet :</strong> " + uri + "</p>");
        out.println("            <p><strong>Context Path :</strong> " + contextPath + "</p>");
        out.println("            <p><strong>Méthode HTTP :</strong> " + req.getMethod() + "</p>");
        out.println("        </div>");
        out.println("        <div class='badge'>Sprint 1 - 4 octobre 2024</div>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
        
        out.flush();
    }
}