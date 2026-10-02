# Terraform — CafeGestión360 en AWS Academy Learner Lab

Crea una sola EC2 (Ubuntu 22.04) que corre todo el stack con Docker Compose
(los 8 microservicios + bff-gateway + frontend + RabbitMQ cluster + Postgres),
más un API Gateway (HTTP API) delante del BFF. Pensado para el Learner Lab:
usa el key pair `vockey` y el `LabInstanceProfile` ya existentes, y no crea
ningún rol ni política IAM (el Lab no lo permite).

Ver el diseño completo en [`docs/EP2_PLAN.md`](../../docs/EP2_PLAN.md),
sección 8.

## Qué crea

| Archivo | Recursos |
|---|---|
| `versions.tf` | providers `aws` y `random` |
| `variables.tf` | todas las variables de entrada |
| `main.tf` | AMI Ubuntu 22.04, Security Group, Elastic IP, la instancia EC2 (disco gp3 30 GB, `user_data`) |
| `monitoring.tf` | alarmas CloudWatch de auto-recuperación/reinicio |
| `apigateway.tf` | API Gateway HTTP API → proxy hacia el BFF en el puerto 8080, inyectando `X-Origin-Verify` |
| `user_data.sh.tftpl` | script de primer arranque: Docker, swap, clona el repo, genera `.env` y el certificado autofirmado, crea el servicio systemd `cafeteria.service` |
| `outputs.tf` | IP elástica, URLs, comando SSH, secreto de `X-Origin-Verify` |

## Requisitos

- Terraform >= 1.5.
- Una sesión activa de **AWS Academy Learner Lab** con credenciales en
  `~/.aws/credentials` (incluye `aws_session_token`; caducan con cada sesión).
- Tu IP pública (para `my_ip_cidr`).
- Los datos de tus App Registrations de Azure (sección 7 / 7-bis del plan).

## Uso

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars   # completa tus valores reales
terraform init
terraform plan
terraform apply                                 # escribe "yes"
terraform output                                # IP, URLs
```

Después del `apply`:

1. Agrega `https://<elastic_ip>` a los Redirect URIs del SPA en Azure
   (sección 7, paso 8).
2. Abre `https://<elastic_ip>` y acepta la advertencia del certificado
   autofirmado (es autofirmado a propósito, no hay dominio propio).
3. UI de RabbitMQ: `http://<elastic_ip>:15672` (solo visible desde tu IP).
4. El frontend y el `.env` del backend ya quedan apuntando al API Gateway
   (`terraform output api_gateway_url`): no hace falta tocar nada a mano.

### Ver el arranque (tarda 10–15 min la primera vez, compila todo)

```bash
ssh -i labsuser.pem ubuntu@<elastic_ip> 'sudo tail -f /var/log/cloud-init-output.log'
ssh -i labsuser.pem ubuntu@<elastic_ip> 'cd /opt/cafeteria && sudo docker compose ps'
```

### Actualizar después de un `git push`

```bash
ssh -i labsuser.pem ubuntu@<elastic_ip> \
  'cd /opt/cafeteria && sudo git pull && sudo docker compose up -d --build'
```

### Validar sin credenciales de AWS (CI o antes de pedirlas)

```bash
terraform init -backend=false
terraform validate
terraform fmt -check
```

### Al terminar el semestre

```bash
terraform destroy
```

## Notas de diseño

- **Sin Elastic Load Balancer ni Auto Scaling**: una sola instancia alcanza
  para la demo y evita costos/roles IAM extra que el Learner Lab no permite
  crear.
- **`X-Origin-Verify`**: el secreto lo genera Terraform (`random_password`) y
  lo inyecta el API Gateway en cada petición (parameter mapping de la
  integración HTTP_PROXY, solo soportado con `payload_format_version = "1.0"`)
  y en el `.env` de la EC2. El BFF (`OriginVerifyGlobalFilter`) lo compara y
  devuelve 403 si alguien le pega directo al puerto 8080 (prueba S8 de
  `docs/EP2_PLAN.md` sección 11).
- **Elastic IP antes que la instancia**: se reserva con `aws_eip` (sin
  asociar) para poder pasar su IP al `user_data` de la propia instancia sin
  crear una dependencia circular; `aws_eip_association` la asocia después.
- **`enable_recovery_alarms`**: el Learner Lab a veces no permite acciones de
  alarma `aws:recover`/`aws:reboot`; si `terraform apply` falla en
  `monitoring.tf`, pon esta variable en `false` y vuelve a aplicar.
- **systemd en vez de `restart` de Docker a secas**: el Learner Lab apaga la
  instancia al cerrar la sesión del lab. `cafeteria.service` (habilitado con
  `systemctl enable`) hace `docker compose up -d --build` en cada arranque,
  así que al reabrir el lab todo vuelve a subir solo, con la misma Elastic IP.
