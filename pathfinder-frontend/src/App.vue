<template>
  <v-app v-if="loggedIn">
    <!-- App Bar -->
    <v-app-bar image="@/assets/webcover0.png">
      <v-row align="center" class="w-100">
        <v-col class="d-flex align-center" cols="auto">
          <v-app-bar-nav-icon @click.stop="drawer = !drawer" />
          <router-link to="/">
            <img alt="Logo" class="mt-1 mr-1" height="50" src="@/assets/Pathfinder_Logo_1.0.png" width="50" />
          </router-link>
          <router-link to="/" class="no-underline">
            <v-toolbar-title class="font-weight-bold text-black">
              Pathfinder
            </v-toolbar-title>
          </router-link>
        </v-col>
        <v-spacer></v-spacer>
        <v-col class="d-flex align-center justify-end" cols="auto" style="padding-right: 16px;">
          <BaseButtonLogout :onLogout="logout" />
        </v-col>
      </v-row>
    </v-app-bar>

    <!-- Navigation Drawer -->
    <v-navigation-drawer v-model="drawer" app class="drawer-background">
      <v-list>
        <v-list-item
          v-for="item in menuItems"
          :key="item.title"
          @click="router.push(item.route)"
          class="menu-item-hover"
        >
          <v-list-item-content>
            <v-list-item-title class="text-black">
              {{ item.title }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <!-- Main Content -->
    <v-main class="main-background">
      <router-view v-slot="{ Component }">
        <v-fade-transition mode="out-in">
          <component :is="Component" />
        </v-fade-transition>
      </router-view>

    </v-main>
  </v-app>

  <!-- Login View -->
  <div v-else>
    <router-link to="/login">Bitte einloggen</router-link>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import BaseButtonLogout from '@/components/common/BaseButtonLogout.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const drawer = ref(true)
const menuItems = ref([
  { title: 'Startseite', route: '/' },
  { title: 'Stellenportal', route: '/stellen/JobpostingOverviewView' },
  { title: 'Bewerbungen', route: '/bewerbungen/ApplicationListView' },
  { title: 'Mein Konto', route: '/nachwuchskraefte/NwkUserinformationView' },
  { title: 'Meine Liste', route: '/merken/BookmarkView' },
])

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
