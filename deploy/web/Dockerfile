# Angular application served by Caddy, which also forwards /api to the
# backend and takes care of HTTPS (see deploy/Caddyfile).
# Build context is the repository root (see docker-compose.prod.yml).

# ---- build the Angular application ----
FROM node:20-alpine AS build
WORKDIR /app
COPY front/smart-company-app/package.json front/smart-company-app/package-lock.json ./
RUN npm ci
COPY front/smart-company-app/ ./
RUN npm run build -- --configuration production

# ---- serve it ----
FROM caddy:2-alpine
COPY deploy/Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/dist/smart-company-app/browser /srv/app
