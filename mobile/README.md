# Servify Mobile

App movil en Expo/React Native para publicar en Android e iOS usando el mismo backend de Servify.

## Ejecutar en desarrollo

1. Instalar dependencias:
   ```bash
   npm install
   ```

2. Configurar la API:
   ```bash
   copy .env.example .env
   ```
   Cambia `EXPO_PUBLIC_SERVIFY_API_URL` por una URL accesible desde el celular o emulador.

3. Levantar:
   ```bash
   npm run android
   npm run ios
   ```

Para Play Store y App Store, el backend no puede quedar como `localhost`; tiene que estar publicado o accesible desde internet.
