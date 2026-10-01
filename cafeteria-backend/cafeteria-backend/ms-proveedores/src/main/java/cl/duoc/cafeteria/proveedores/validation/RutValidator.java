package cl.duoc.cafeteria.proveedores.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implementa el calculo del digito verificador de un RUT chileno (modulo 11).
 *
 * Pasos:
 * 1) Se eliminan los puntos y espacios del valor recibido.
 * 2) Se valida el formato "cuerpo-dv" (cuerpo: 1 a 8 digitos; dv: digito o K/k).
 * 3) Se recorre el cuerpo de derecha a izquierda multiplicando cada digito por
 *    una secuencia ciclica 2,3,4,5,6,7 (vuelve a 2 despues del 7).
 * 4) Se suman los productos, se calcula 11 - (suma % 11):
 *    - si el resultado es 11 -> digito verificador esperado es 0
 *    - si el resultado es 10 -> digito verificador esperado es K
 *    - en otro caso -> el resultado mismo
 * 5) Se compara (ignorando mayusculas/minusculas) con el dv recibido.
 */
public class RutValidator implements ConstraintValidator<Rut, String> {

    private static final Pattern FORMATO_RUT = Pattern.compile("^(\\d{1,8})-([0-9kK])$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            // La obligatoriedad del campo la valida @NotBlank, no esta anotacion.
            return true;
        }

        String normalizado = value.replace(".", "").trim();
        var matcher = FORMATO_RUT.matcher(normalizado);
        if (!matcher.matches()) {
            return false;
        }

        String cuerpo = matcher.group(1);
        char dvRecibido = Character.toUpperCase(matcher.group(2).charAt(0));
        char dvEsperado = calcularDigitoVerificador(cuerpo);

        return dvRecibido == dvEsperado;
    }

    private char calcularDigitoVerificador(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(cuerpo.charAt(i));
            suma += digito * multiplicador;
            multiplicador++;
            if (multiplicador > 7) {
                multiplicador = 2;
            }
        }

        int resultado = 11 - (suma % 11);
        if (resultado == 11) {
            return '0';
        }
        if (resultado == 10) {
            return 'K';
        }
        return Character.forDigit(resultado, 10);
    }
}
