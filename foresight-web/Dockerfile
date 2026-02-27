# Estágio 1: Build (Compilação)
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build --configuration=production

# Estágio 2: Serve (Servidor Nginx)
FROM nginx:alpine

# Limpa a pasta padrão do Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copia os arquivos compilados do Angular (Ajuste o caminho se a versão do seu Angular for mais antiga e não usar a pasta /browser)
COPY --from=build /app/dist/foresight-web/browser /usr/share/nginx/html

# Copia a configuração customizada do Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
