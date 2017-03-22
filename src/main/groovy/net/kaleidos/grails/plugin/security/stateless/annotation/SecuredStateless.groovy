package net.kaleidos.grails.plugin.security.stateless.annotation

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Indicates that a service method (or entire controller) requires
 * being called with an Authorization header, using the grails-security-stateles plugin.
 */
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredStateless {

  /**
   * Whether to display an error or be silent (default).
   * @return  <code>true</code> if an error should be shown
   */
  boolean error() default false;
}
