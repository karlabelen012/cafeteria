# Dockerfile generico para cualquiera de los 8 microservicios + el bff-gateway.
# Se usa siempre con el contexto de build apuntando a esta carpeta (cafeteria-backend/)
# y el build-arg MODULE indicando el modulo Maven a compilar y empaquetar
# (ver docker-compose.yml: cada servicio pasa su propio "args: MODULE: ...").

FROM eclipse-temurin:17-jdk-alpine AS build
ARG MODULE
WORKDIR /workspace

# Copiamos solo los pom.xml primero (de todo el reactor multi-modulo, aunque
# solo compilemos uno) para que Docker cachee la resolucion de dependencias
# mientras no cambien los pom.xml.
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY ms-productos/pom.xml ms-productos/pom.xml
COPY ms-inventario/pom.xml ms-inventario/pom.xml
COPY ms-pedidos/pom.xml ms-pedidos/pom.xml
COPY ms-clientes/pom.xml ms-clientes/pom.xml
COPY ms-pagos/pom.xml ms-pagos/pom.xml
COPY ms-empleados/pom.xml ms-empleados/pom.xml
COPY ms-proveedores/pom.xml ms-proveedores/pom.xml
COPY ms-reportes/pom.xml ms-reportes/pom.xml
COPY bff-gateway/pom.xml bff-gateway/pom.xml
RUN chmod +x mvnw && ./mvnw -q -B -pl ${MODULE} -am dependency:go-offline

# Ahora si copiamos el codigo fuente del modulo pedido y lo empaquetamos.
COPY ${MODULE}/src ${MODULE}/src
RUN ./mvnw -q -B -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:17-jre-alpine
ARG MODULE
WORKDIR /app
COPY --from=build /workspace/${MODULE}/target/${MODULE}.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
