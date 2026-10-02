output "elastic_ip" {
  description = "IP pública fija de la EC2 (no cambia aunque el Learner Lab detenga/reinicie la instancia)."
  value       = aws_eip.app.public_ip
}

output "frontend_url" {
  description = "URL del frontend (nginx en la EC2, HTTPS con certificado autofirmado: el navegador pedirá aceptar la excepción)."
  value       = "https://${aws_eip.app.public_ip}"
}

output "api_gateway_url" {
  description = "URL pública del API Gateway. Es el valor que va en VITE_API_BASE_URL del frontend."
  value       = "${aws_apigatewayv2_api.app.api_endpoint}/api"
}

output "rabbitmq_management_url" {
  description = "UI de administración de RabbitMQ del nodo 1 (solo accesible desde my_ip_cidr)."
  value       = "http://${aws_eip.app.public_ip}:15672"
}

output "ssh_command" {
  description = "Comando para conectarse por SSH a la instancia con la llave del Learner Lab."
  value       = "ssh -i labsuser.pem ubuntu@${aws_eip.app.public_ip}"
}

output "origin_verify_secret" {
  description = "Secreto generado para el header X-Origin-Verify entre el API Gateway y el BFF (sensible: no se imprime en la consola salvo con 'terraform output origin_verify_secret')."
  value       = random_password.origin_verify_secret.result
  sensitive   = true
}
