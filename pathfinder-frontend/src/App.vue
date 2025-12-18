<template>
  <v-app>
    <!-- APP LAYOUT -->
    <v-app-bar app image="@/assets/webcover0.png">
      <v-row align="center" class="w-100">
        <v-col cols="auto" class="d-flex align-center">
          <v-app-bar-nav-icon @click.stop="drawer = !drawer" />
          <router-link to="/" class="no-underline">
            <img src="@/assets/Pathfinder_Logo_1.0.png" height="50" />
          </router-link>
          <router-link to="/" class="no-underline">
            <v-toolbar-title class="font-weight-bold text-black">Pathfinder</v-toolbar-title>
          </router-link>
        </v-col>

        <v-spacer />

        <v-col cols="auto">
          <BaseButtonLogout :onLogout="logout" />
        </v-col>
      </v-row>
    </v-app-bar>

    <v-navigation-drawer v-model="drawer" app class="drawer-background">
      <v-list>
        <v-list-item v-for="item in menuItems" :key="item.title" @click="router.push(item.route)">
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <!-- CONTENT -->
    <v-main class="main-background pa-0">
      <router-view />
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import BaseButtonLogout from '@/components/common/BaseButtonLogout.vue'
import { mountLogin } from '@/main'

const router = useRouter()
const drawer = ref(true)
const menuItems = [
  { title: 'Startseite', route: '/' },
  { title: 'Stellenportal', route: '/stellen/JobpostingOverviewView' },
  { title: 'Bewerbungen', route: '/bewerbungen/ApplicationListView' },
  { title: 'Mein Konto', route: '/nachwuchskraefte/NwkUserinformationView' },
  { title: 'Meine Liste', route: '/merken/BookmarkView' },
]

const isLoggedIn = computed(() => sessionStorage.getItem('loggedIn') === 'true')

function logout() {
  sessionStorage.clear()
  mountLogin()
  history.replaceState(null, '', '/LoginView')
}
</script>

<style scoped>
/* Hauptfarbe für Text */
.main-background { background-color: #ffffff; color: #000000; min-height: 100vh; }
.drawer-background { background-color: #EEE9E9; }

/* Pathfinder-Text */
.v-toolbar-title, .v-toolbar-title a {
  color: black !important;
  text-decoration: none !important;
}

/* Menüitems Text schwarz & Hover */
.v-list-item .v-list-item-title {
  color: black !important;
}

.menu-item-hover:hover {
  background-color: rgba(0, 0, 0, 0.05);
  cursor: pointer;
}

.no-underline, .no-underline:hover {
  text-decoration: none;
}

.customfooter {
  justify-content: center;
}
</style>
