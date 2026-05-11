import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
  plugins: [react()],
  ...(command === 'serve'
    ? {
        server: {
          proxy: {
            '/api': 'http://localhost:8080',
            '/oauth2': 'http://localhost:8080',
            '/login': 'http://localhost:8080',
          },
        },
      }
    : {}),
}))
