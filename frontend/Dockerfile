# ===================================================================
# DOCKERFILE PARA FRONTEND (REACT + VITE + NGINX)
# ===================================================================

# Etapa 1: Compilación de la aplicación React con Vite
FROM node:20-alpine AS builder
WORKDIR /app

# Instalar dependencias
COPY package.json package-lock.json* ./
RUN npm install

# Copiar código fuente y construir bundle de producción
COPY . .
RUN npm run build

# Etapa 2: Servidor web ultraligero Nginx para servir archivos estáticos
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Limpiar archivos por defecto de nginx
RUN rm -rf ./*

# Copiar el bundle compilado de Vite
COPY --from=builder /app/dist .

# Copiar la configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer el puerto HTTP interno del contenedor
EXPOSE 80

# Comando de inicio del servidor Nginx
CMD ["nginx", "-g", "daemon off;"]
