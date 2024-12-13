package mg.itu.framework.annotation;

import java.lang.annotation.*;

/**
 * SPRINT 2 : Annotation pour marquer les contrôleurs et les méthodes
 * 
 * Utilisation :
 * - Sur une classe : @MyAnnotation (marque comme contrôleur)
 * - Sur une méthode : @MyAnnotation(value="/url", method=HttpMethod.GET)
 * 
 * @version Sprint 2 - 11 octobre 2024
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface MyAnnotation {
    String value() default "";
    HttpMethod method() default HttpMethod.GET;
}