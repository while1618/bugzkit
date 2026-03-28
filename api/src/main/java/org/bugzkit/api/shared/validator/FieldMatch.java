package org.bugzkit.api.shared.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.bugzkit.api.shared.validator.impl.FieldMatchImpl;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = FieldMatchImpl.class)
@Documented
public @interface FieldMatch {
  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String first();

  String second();

  @Target({TYPE, ANNOTATION_TYPE})
  @Retention(RUNTIME)
  @Documented
  @interface List {
    FieldMatch[] value();
  }
}
