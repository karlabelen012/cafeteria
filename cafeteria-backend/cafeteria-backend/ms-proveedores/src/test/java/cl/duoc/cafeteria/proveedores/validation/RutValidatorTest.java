package cl.duoc.cafeteria.proveedores.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifica el calculo del digito verificador (modulo 11) contra RUT cuyo
 * digito verificador fue calculado a mano/con script antes de escribir el
 * validador (ver tabla en el metodo de la clase, igual que en
 * config/ProveedorSeedData):
 *
 * cuerpo     -> dv esperado
 * 12345678   -> 5
 * 76543210   -> 3
 * 9867654    -> 6
 * 20123456   -> 5
 * 18765432   -> 7
 */
class RutValidatorTest {

    private RutValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new RutValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void rutsValidosConDvCorrectoPasan() {
        assertThat(validator.isValid("12345678-5", context)).isTrue();
        assertThat(validator.isValid("76543210-3", context)).isTrue();
        assertThat(validator.isValid("9867654-6", context)).isTrue();
        assertThat(validator.isValid("20123456-5", context)).isTrue();
        assertThat(validator.isValid("18765432-7", context)).isTrue();
    }

    @Test
    void aceptaFormatoConPuntosYKMinuscula() {
        assertThat(validator.isValid("12.345.678-5", context)).isTrue();
        assertThat(validator.isValid("76.543.210-3", context)).isTrue();
    }

    @Test
    void nullYBlancoSonValidosParaEstaAnotacion() {
        // La obligatoriedad la valida @NotBlank, no @Rut.
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("", context)).isTrue();
        assertThat(validator.isValid("   ", context)).isTrue();
    }

    @Test
    void digitoVerificadorIncorrectoEsInvalido() {
        // El dv correcto de 12345678 es 5, no 9.
        assertThat(validator.isValid("12345678-9", context)).isFalse();
        // El dv correcto de 76543210 es 3, no K.
        assertThat(validator.isValid("76543210-K", context)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123456785",       // sin guion
            "12345678",        // sin digito verificador
            "ABCDEFGH-5",      // cuerpo no numerico
            "12345678-",       // dv vacio
            "12345678--5",     // doble guion
            "123456789012-5"   // cuerpo demasiado largo
    })
    void formatoMalformadoEsInvalido(String rut) {
        assertThat(validator.isValid(rut, context)).isFalse();
    }
}
