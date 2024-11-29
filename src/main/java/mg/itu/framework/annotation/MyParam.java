package mg.itu.framework.annotation;

import java.lang.annotation.*;

/**
 * SPRINT 6bis : Annotation @MyParam (équivalent de @RequestParam)
 * 
 * Permet de spécifier le nom du paramètre à récupérer dans la requête.
 * 
 * Exemple :
 * public String test(@MyParam("nbr") int nombre)
 * -> Récupère request.getParameter("nbr") et le convertit en int
 * 
 * @version Sprint 6bis - 22 novembre 2024
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MyParam {
    String value();
}