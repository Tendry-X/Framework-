package mg.itu.framework.servlet;

import mg.itu.framework.annotation.MyAnnotation;
import mg.itu.framework.annotation.MyParam;
import mg.itu.framework.model.ModelView;
import mg.itu.framework.model.Url;
import mg.itu.framework.util.AnotationReader;
import mg.itu.framework.util.Transtipation;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

@WebServlet("/")
public class UrlListener extends HttpServlet {

    private RequestDispatcher defaultDispatcher;
    private Map<Url, Map<Class<?>, Method>> classes;

    @Override
    public void init() {
        defaultDispatcher = getServletContext().getNamedDispatcher("default");
        AnotationReader a = new AnotationReader();
        try {
            classes = a.getClassesWithAnnotation(getServletContext());
            // Affiche la map pour debug
        } catch (Exception e) {
            e.printStackTrace();
            classes = new HashMap<>();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doService(req, res);
    }

    public void doService(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        try {
            checkannotation(req, res, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkannotation(HttpServletRequest req, HttpServletResponse res, String path) throws Exception {
        Map.Entry<Map<Class<?>, Method>, List<String>> matchEntry = AnotationReader.getMatchesWithParams(path, classes,
                req.getMethod());

        if (matchEntry != null) {
            Map<Class<?>, Method> match = matchEntry.getKey();
            List<String> pathParams = matchEntry.getValue();

            for (Map.Entry<Class<?>, Method> entry : match.entrySet()) {
                Class<?> clazz = entry.getKey();
                Method method = entry.getValue();

                try {
                    if (method.getReturnType().equals(String.class)) {
                        res.setContentType("text/html;charset=UTF-8");
                        PrintWriter out = res.getWriter();
                        out.print(method.invoke(clazz.getDeclaredConstructor().newInstance()));
                        out.flush();

                    } else if (method.getReturnType().equals(ModelView.class)) {

                        Object[] obj = new Object[method.getParameters().length];
                        Parameter[] params = method.getParameters();

                        for (int i = 0; i < params.length; i++) {
                            if (params[i].isAnnotationPresent(MyParam.class)) {
                                MyParam annotation = params[i].getAnnotation(MyParam.class);
                                String paramName = annotation.value();
                                String paramValue = req.getParameter(paramName);
                                obj[i] = Transtipation.convert(paramValue, params[i].getType());

                            } else if (i < pathParams.size()) {
                                obj[i] = Transtipation.convert(pathParams.get(i), params[i].getType());

                            } else {
                                String paramValue = req.getParameter(params[i].getName());
                                obj[i] = Transtipation.convert(paramValue, params[i].getType());
                            }
                        }

                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        ModelView mv = (obj.length > 0) ? (ModelView) method.invoke(instance, obj)
                                : (ModelView) method.invoke(instance);

                        String view = "/WEB-INF/" + mv.getJspName() + ".jsp";

                        if (mv.getData() != null) {
                            for (Map.Entry<String, Object> dataEntry : mv.getData().entrySet()) {
                                req.setAttribute(dataEntry.getKey(), dataEntry.getValue());
                            }
                        }

                        req.getRequestDispatcher(view).forward(req, res);
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            notFound(req, res, path);
        }
    }

    private void notFound(HttpServletRequest req, HttpServletResponse res, String path)
            throws IOException, ServletException {
        try (PrintWriter out = res.getWriter()) {
            res.setContentType("text/html;charset=UTF-8");
            out.println("<h3>404 Not Found</h3>");
            out.println("Real path   " + getServletContext().getRealPath("/"));
            out.println("context path   " + getServletContext().getContextPath());
            out.println("resources path   " + getServletContext().getResourcePaths("/"));
            out.println("<p>Aucune correspondance pour : " + path + "</p>");
            out.flush();

        }
    }

    // Optionnel : mÃ©thode pour afficher dans le navigateur via out.println
    private void printAnnotatedMapHttp(HttpServletResponse res) throws IOException {
        res.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = res.getWriter()) {
            if (classes == null || classes.isEmpty()) {
                out.println("<p>La map est vide</p>");
                return;
            }
            out.println("<h2>Contenu de la map annotÃ©e :</h2>");
            out.println("<ul>");
            for (Map.Entry<Url, Map<Class<?>, Method>> entry : classes.entrySet()) {
                Url url = entry.getKey();
                Map<Class<?>, Method> map = entry.getValue();

                out.println("<li><strong>URL:</strong> " + url.getUrlpattern() +
                        " [MÃ©thode HTTP: " + url.getMethod() + "]<ul>");
                for (Map.Entry<Class<?>, Method> cm : map.entrySet()) {
                    out.println("<li>Classe: " + cm.getKey().getName() +
                            " | MÃ©thode: " + cm.getValue().getName() + "</li>");
                }
                out.println("</ul></li>");
            }
            out.println("</ul>");
            out.flush();
        }
    }
}
