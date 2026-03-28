package org.bugzkit.api.shared.validator.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.bugzkit.api.shared.validator.FieldMatch;
import org.springframework.beans.BeanWrapperImpl;

public class FieldMatchImpl implements ConstraintValidator<FieldMatch, Object> {
  private String firstFieldName;
  private String secondFieldName;

  @Override
  public void initialize(FieldMatch constraint) {
    firstFieldName = constraint.first();
    secondFieldName = constraint.second();
  }

  public boolean isValid(Object obj, ConstraintValidatorContext context) {
    final var wrapper = new BeanWrapperImpl(obj);
    final var first = (String) wrapper.getPropertyValue(firstFieldName);
    final var second = (String) wrapper.getPropertyValue(secondFieldName);

    return first == null && second == null || first != null && first.equals(second);
  }
}
