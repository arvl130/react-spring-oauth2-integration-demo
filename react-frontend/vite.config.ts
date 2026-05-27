import path from "path";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());

  return {
    server: {
      proxy: {
        "/oauth2/authorization/keycloak": {
          target: env.VITE_API_BASE_URL,
          secure: false,
        },
        "/login/oauth2/code/keycloak": {
          target: env.VITE_API_BASE_URL,
          secure: false,
        },
        "/logout": {
          target: env.VITE_API_BASE_URL,
          secure: false,
        },
        "/api": {
          target: env.VITE_API_BASE_URL,
          secure: false,
        },
      },
    },
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
  };
});
