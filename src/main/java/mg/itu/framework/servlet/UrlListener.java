package mg.itu.framework.servlet;

import mg.itu.framework.annotation.MyAnnotation;

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

 * SPRINT 2 : Servlet avec scan d'annotations

 * 

 * Scanne les classes avec @MyAnnotation et exécute les méthodes correspondantes

 * 

 * @version Sprint 2 - 11 octobre 2024

 */

public class UrlListener extends HttpServlet {

    private Map<Url, Map<Class<?>, Method>> classes;

    @Override

    public void init() {

        AnotationReader a = new AnotationReader();

        try {

            classes = a.getClassesWithAnnotation(getServletContext());

        } catch (Exception e) {

            e.printStackTrace();

            classes = new HashMap<>();

        }

    }

    @Override

    protected void service(HttpServletRequest req, HttpServletResponse res)

            throws ServletException, IOException {

        String path = req.getRequestURI().substring(req.getContextPath().length());

        

        try {

            Map<Class<?>, Method> match = AnotationReader.getMatches(path, classes, req.getMethod());

            

            if (match != null) {

                // Méthode trouvée, l'exécuter

                for (Map.Entry<Class<?>, Method> entry : match.entrySet()) {

                    Class<?> clazz = entry.getKey();

                    Method method = entry.getValue();

                    

                    try {

                        Object instance = clazz.getDeclaredConstructor().newInstance();

                        

                        // Si retourne String, l'afficher directement

                        if (method.getReturnType().equals(String.class)) {

                            res.setContentType("text/html;charset=UTF-8");

                            PrintWriter out = res.getWriter();

                            out.print(method.invoke(instance));

                            out.flush();

                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                    }

                }

            } else {

                // Aucune correspondance

                notFound(req, res, path);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private void notFound(HttpServletRequest req, HttpServletResponse res, String path)

            throws IOException {

        res.setContentType("text/html;charset=UTF-8");

        PrintWriter out = res.getWriter();

        out.println("<h3>404 Not Found</h3>");

        out.println("<p>Aucune correspondance pour : " + path + "</p>");

        out.println("<p><em>Sprint 2 - 11 octobre 2024</em></p>");

        out.flush();

    }

}