# Backend remoto (S3 + bloqueo con DynamoDB). Vacío a propósito: el bucket,
# la tabla y la región se pasan en tiempo de "terraform init" con
# -backend-config (ver init-backend.sh), nunca hardcodeados aquí, porque el
# nombre del bucket depende del Account ID de quien lo corra (AWS Academy
# Learner Lab asigna una cuenta nueva por estudiante/sesión).
#
# Por qué hace falta un backend remoto: GitHub Actions corre cada workflow en
# un runner nuevo y vacío. Sin un backend remoto, cada "terraform apply" de
# CI no encontraría el state del run anterior y volvería a crear la EC2, el
# Security Group, la Elastic IP y el API Gateway desde cero, duplicando todo
# y dejando huérfanos los recursos previos (ver infra/terraform/README.md,
# sección "CI/CD con GitHub Actions").
#
# Para desarrollo local SOLO de validación (sin aplicar nada) seguís pudiendo
# usar "terraform init -backend=false" (ver README): ese flag ignora este
# bloque por completo.
terraform {
  backend "s3" {}
}
