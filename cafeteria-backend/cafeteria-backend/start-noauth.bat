@echo off
REM Levanta los 8 microservicios + el BFF en modo noauth (H2, sin Docker),
REM cada uno en su propia ventana para poder ver sus logs por separado.
REM Requisito unico: Java 17 instalado (el propio Maven lo descarga mvnw solo).

setlocal
set SPRING_PROFILES_ACTIVE=noauth
set FRONTEND_ORIGIN=http://localhost:4200
cd /d "%~dp0"

echo Iniciando microservicios...
start "ms-productos (8081)"   cmd /k mvnw.cmd -f ms-productos\pom.xml spring-boot:run
start "ms-inventario (8082)"  cmd /k mvnw.cmd -f ms-inventario\pom.xml spring-boot:run
start "ms-pedidos (8083)"     cmd /k mvnw.cmd -f ms-pedidos\pom.xml spring-boot:run
start "ms-clientes (8084)"    cmd /k mvnw.cmd -f ms-clientes\pom.xml spring-boot:run
start "ms-pagos (8085)"       cmd /k mvnw.cmd -f ms-pagos\pom.xml spring-boot:run
start "ms-empleados (8086)"   cmd /k mvnw.cmd -f ms-empleados\pom.xml spring-boot:run
start "ms-proveedores (8087)" cmd /k mvnw.cmd -f ms-proveedores\pom.xml spring-boot:run
start "ms-reportes (8088)"    cmd /k mvnw.cmd -f ms-reportes\pom.xml spring-boot:run

echo Esperando 25 segundos a que los microservicios terminen de arrancar...
timeout /t 25 /nobreak > nul

echo Iniciando el BFF gateway...
start "bff-gateway (8080)" cmd /k mvnw.cmd -f bff-gateway\pom.xml spring-boot:run

echo.
echo Listo. Se abrieron 9 ventanas (una por servicio). Para probar en un rato:
echo   http://localhost:8080/api/productos
echo.
echo Para detener todo, simplemente cierra las 9 ventanas.
pause
