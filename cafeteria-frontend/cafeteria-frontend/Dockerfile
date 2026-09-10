# Imagen de desarrollo/demo para el frontend React + Vite.
# Las variables VITE_* se "hornean" en el bundle en tiempo de build (asi
# funciona Vite), por eso se reciben como build-args en vez de variables de
# entorno normales de contenedor (ver docker-compose.yml en la raiz del repo).

FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

ARG VITE_AUTH_DISABLED=true
ARG VITE_API_BASE_URL=http://localhost:8080/api
ARG VITE_AZURE_CLIENT_ID=placeholder
ARG VITE_AZURE_TENANT_ID=placeholder
ARG VITE_AZURE_REDIRECT_URI=http://localhost:4200
ARG VITE_API_SCOPES=api://cafeteria-backend/pedidos.read,api://cafeteria-backend/pedidos.write
ENV VITE_AUTH_DISABLED=$VITE_AUTH_DISABLED \
    VITE_API_BASE_URL=$VITE_API_BASE_URL \
    VITE_AZURE_CLIENT_ID=$VITE_AZURE_CLIENT_ID \
    VITE_AZURE_TENANT_ID=$VITE_AZURE_TENANT_ID \
    VITE_AZURE_REDIRECT_URI=$VITE_AZURE_REDIRECT_URI \
    VITE_API_SCOPES=$VITE_API_SCOPES

COPY . .
RUN npm run build

EXPOSE 4200
CMD ["npx", "vite", "preview", "--host", "0.0.0.0", "--port", "4200"]
