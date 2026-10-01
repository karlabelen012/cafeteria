package cl.duoc.cafeteria.proveedores.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que un String sea un RUT chileno con digito verificador correcto
 * (algoritmo modulo 11). Acepta formatos como "12345678-9" o "12.345.678-9"
 * (los puntos se ignoran al validar), y "k"/"K" como digito verificador.
 *
 * Un valor null o en blanco se considera VALIDO para esta anotacion: la
 * obligatoriedad del campo es responsabilidad de @NotBlank, no de @Rut.
 */
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RutValidator.class)
public @interface Rut {

    String message() default "El RUT no es valido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
