FROM node:22-alpine AS base
WORKDIR /app
ARG PUBLIC_APP_NAME
ARG PUBLIC_API_URL
ARG JWT_SECRET
COPY package.json .
COPY pnpm-lock.yaml .
RUN npm install -g pnpm
RUN pnpm install
COPY . .

FROM base AS dev
ENV PUBLIC_APP_NAME=$PUBLIC_APP_NAME
ENV PUBLIC_API_URL=$PUBLIC_API_URL
ENV JWT_SECRET=$JWT_SECRET
RUN apk add --no-cache curl
CMD ["pnpm", "run", "dev", "--host"]

FROM base AS build
RUN pnpm run build
RUN pnpm prune --prod

FROM node:22-alpine AS prod
RUN apk add --no-cache curl
WORKDIR /app
# copy node_modules only if you have dependencies in package.json
# if you only have devDependencies you can skip this line
# COPY --from=build /app/node_modules node_modules/
COPY --from=build /app/build build/
COPY --from=build /app/package.json .
ENV NODE_ENV=production
CMD ["node", "build"]
