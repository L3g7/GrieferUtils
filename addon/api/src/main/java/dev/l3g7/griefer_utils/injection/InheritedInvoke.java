package dev.l3g7.griefer_utils.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a mixin @Inject targeting the invocation of an inherited method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface InheritedInvoke {

	/**
	 * The class declaring the targeted method.
	 */
	Class<?> value();

}
