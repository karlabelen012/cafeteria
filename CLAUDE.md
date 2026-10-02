Proyecto: CafeGestión360 (DSY1107). Backend: Java 17, Spring Boot 3.3.x, Maven multi-módulo en
cafeteria-backend/cafeteria-backend. Frontend: React 18 + Vite en cafeteria-frontend/cafeteria-frontend.
Especificación completa: docs/EP2_PLAN.md. Ejecuta SOLO la fase que se te pida.

Reglas:
1. No rompas lo que ya funciona: seguridad JWT con Azure Entra ID, perfil noauth, Docker Compose.
2. Al terminar cada fase deben pasar: `./mvnw -q clean verify` (backend) y `npm run build` (frontend).
   Si algo falla, arréglalo antes de terminar.
3. Nombres de colas, exchanges y routing keys SOLO en application.yml (bloque app.rabbitmq) +
   RabbitProperties + RabbitMQConfig. Nunca strings sueltos en services, controllers ni listeners.
4. Controllers sin lógica de negocio ni de RabbitMQ. Services sin RabbitTemplate (usan interfaces *EventPublisher).
5. Listeners con @RabbitListener, ACK manual explícito según docs/EP2_PLAN.md §3.5, agrupados por dominio
   en messaging/consumer/<subdominio>.
6. Nunca subas secretos: solo .env.example y terraform.tfvars.example con placeholders.
7. Nunca guardes número de tarjeta completo ni CVV. Nunca hagas console.log de tokens.
8. Comentarios y mensajes de error en español. Código limpio, sin código muerto.
9. Al final de cada fase, entrega un resumen: archivos cambiados, cómo probarlo (comandos curl) y
   qué queda pendiente. Actualiza README.md con lo nuevo.
