FROM docker.io/library/node:22-alpine AS build
WORKDIR /w
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM docker.io/nginxinc/nginx-unprivileged:1.27-alpine
COPY --from=build /w/dist/ /usr/share/nginx/html/
RUN printf 'server {\n listen 8080;\n root /usr/share/nginx/html;\n location / { try_files $uri $uri/ /index.html; }\n}\n' > /etc/nginx/conf.d/default.conf
EXPOSE 8080
