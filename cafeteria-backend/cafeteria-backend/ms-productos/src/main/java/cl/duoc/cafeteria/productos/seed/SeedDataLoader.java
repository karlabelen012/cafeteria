package cl.duoc.cafeteria.productos.seed;

import cl.duoc.cafeteria.productos.model.Producto;
import cl.duoc.cafeteria.productos.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga los 12 productos de demostracion (los que ya tienen foto en
 * cafeteria-frontend/public/productos/) para que la tienda y el dashboard
 * no partan vacios. Se activa SOLO con el perfil "seed" (ver docs/EP2_PLAN.md
 * seccion 5), combinable con otros perfiles, p.ej. SPRING_PROFILES_ACTIVE=noauth,seed.
 * No hace nada si ya existen productos (para no duplicar en cada reinicio).
 */
@Component
@Profile("seed")
public class SeedDataLoader implements CommandLineRunner {

    private final ProductoRepository repository;

    public SeedDataLoader(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                producto("Latte Vainilla", "Espresso suave con leche vaporizada y un toque de vainilla.",
                        3200.0, "Bebidas calientes", "/productos/latte-vainilla.jpg"),
                producto("Mocaccino", "Espresso con chocolate, leche vaporizada y un toque de cacao.",
                        3400.0, "Bebidas calientes", "/productos/mocaccino.jpg"),
                producto("Cold Brew", "Cafe extraido en frio durante horas, suave y refrescante.",
                        3300.0, "Bebidas frías", "/productos/cold-brew.jpg"),
                producto("Frappé", "Cafe helado batido con hielo, cremoso y bien frio.",
                        3600.0, "Bebidas frías", "/productos/frappe.jpg"),
                producto("Banana Bread", "Queque humedo de platano, horneado todos los dias.",
                        2800.0, "Pastelería", "/productos/banana-bread.jpg"),
                producto("Brownie", "Brownie de chocolate intenso con el centro bien fudgy.",
                        2500.0, "Pastelería", "/productos/brownie.jpg"),
                producto("Croissant de Almendra", "Croissant relleno de crema de almendra y hojuelas tostadas.",
                        2700.0, "Pastelería", "/productos/croissant-de-almendra.jpg"),
                producto("Red Velvet", "Bizcocho rojo aterciopelado con frosting de queso crema.",
                        3000.0, "Pastelería", "/productos/red-velvet.jpg"),
                producto("Cupcake de Chocolate", "Cupcake de chocolate con frosting cremoso.",
                        2200.0, "Pastelería", "/productos/cupcake-de-chocolate.jpg"),
                producto("Cupcake de Limón", "Cupcake de limon fresco con frosting suave.",
                        2200.0, "Pastelería", "/productos/cupcake-de-limon.jpg"),
                producto("Cupcake de Vainilla y Chispas de Chocolate", "Cupcake de vainilla con chispas de chocolate.",
                        2300.0, "Pastelería", "/productos/cupcake-de-vainilla-y-chispas-de-chocolate.jpg"),
                producto("Caja de Galletas Estilo Crumbl", "Caja surtida de galletas estilo Crumbl, recien horneadas.",
                        4500.0, "Galletas", "/productos/caja-de-galletas-estilo-crumbl.jpg")
        ));
    }

    private Producto producto(String nombre, String descripcion, Double precio, String categoria, String imagenUrl) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        producto.setDisponible(true);
        producto.setImagenUrl(imagenUrl);
        return producto;
    }
}
