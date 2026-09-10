#!/bin/bash
# Levanta los 8 microservicios + el BFF en modo noauth (H2, sin Docker),
# todos en segundo plano con sus logs en ./logs/.
# Requisito unico: Java 17 instalado (mvnw descarga Maven solo).
set -e
cd "$(dirname "$0")"
mkdir -p logs

export SPRING_PROFILES_ACTIVE=noauth
export FRONTEND_ORIGIN="http://localhost:4200"

echo "Iniciando microservicios..."
for svc in ms-productos ms-inventario ms-pedidos ms-clientes ms-pagos ms-empleados ms-proveedores ms-reportes; do
  echo "  - $svc"
  nohup ./mvnw -f "$svc/pom.xml" spring-boot:run > "logs/$svc.log" 2>&1 &
  disown
done

echo "Esperando 25 segundos a que los microservicios terminen de arrancar..."
sleep 25

echo "Iniciando el BFF gateway..."
nohup ./mvnw -f bff-gateway/pom.xml spring-boot:run > logs/bff-gateway.log 2>&1 &
disown

echo ""
echo "Listo. Logs en ./logs/. Para probar en un rato:"
echo "  curl http://localhost:8080/api/productos"
echo ""
echo "Para detener todo: kill \$(lsof -ti:8080-8088 2>/dev/null) 2>/dev/null"
echo "(o en Windows/Git Bash: revisa 'netstat -ano | grep LISTENING' y usa taskkill)"
