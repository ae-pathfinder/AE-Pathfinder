import { createApp } from 'vue'
import App from './App.vue'
import LoginView from '@/pages/LoginView.vue'
import { registerPlugins } from './plugins'

let appInstance: any = null

export function mountApp() {
  appInstance?.unmount() // alte App ggf. entladen
  appInstance = createApp(App)
  registerPlugins(appInstance)
  appInstance.mount('#app')
}

export function mountLogin() {
  appInstance?.unmount() // alte App ggf. entladen
  appInstance = createApp(LoginView)
  appInstance.mount('#app')
}

// Prüfen, ob eingeloggt
const loggedIn = sessionStorage.getItem('loggedIn') === 'true'
if (loggedIn) mountApp()
else mountLogin()
