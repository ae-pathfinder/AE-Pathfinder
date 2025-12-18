<template>
  <v-container v-if="bewerbung">
    <!-- Stelle-Titel aus DTO -->
    <h1>Bewerbung: {{ bewerbung.stelleTitel || 'Lädt...' }}</h1>

    <!-- Bewerbungsdetails anzeigen -->
    <BaseCardApplicationProcess
      :bewerbung="bewerbung"
      @open-message="dialogOpen = true"
    />

    <!-- Dialog nur anzeigen, wenn akzeptiert -->
    <BaseDialogMessage v-model="dialogOpen" />

    <v-btn color="primary" @click="goBack" class="mt-4">
      Zurück zur Übersicht
    </v-btn>
  </v-container>

  <!-- Loading / Info -->
  <v-container v-else>
    <v-alert type="info">
      Bewerbung wird geladen...
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import BaseCardApplicationProcess from '@/components/bewerbungen/BaseCardApplicationProcess.vue'
import BaseDialogMessage from '@/components/bewerbungen/BaseDialogMessage.vue'

// Interfaces
interface Bewerbung {
  id: number
  status: 'EINGEREICHT' | 'IN_PRUEFUNG' | 'ABGELEHNT' | 'ANGELADEN' | 'ANGENOMMEN'
  kommentar?: string
  eingereichtAm: string
  nachwuchskraftId: number
  nachwuchskraftName: string
  stelleId: number
  stelleTitel: string
}

// Router
const route = useRoute()
const router = useRouter()
const jobId = Number(route.params.id)

// Daten
const bewerbung = ref<Bewerbung | null>(null)
const dialogOpen = ref(false)
const API_BEW = '/api/bewerbungen'
const nwkId = ref<number | null>(null) // aktuell eingeloggte Nachwuchskraft

onMounted(async () => {
  // Nutzer-Daten aus Session holen
  const userJson = sessionStorage.getItem('user')
  if (!userJson) {
    console.error('Kein eingeloggter Nutzer gefunden')
    router.replace('/LoginView')
    return
  }

  const userData = JSON.parse(userJson)
  nwkId.value = userData.id

  try {
    const res = await axios.get<Bewerbung>(`${API_BEW}/${jobId}`)
    const data = res.data

    // Prüfen, ob die Bewerbung der eingeloggten Nachwuchskraft gehört
    if (data.nachwuchskraftId !== nwkId.value) {
      alert('Du darfst diese Bewerbung nicht ansehen')
      router.replace('/bewerbungen/ApplicationListView')
      return
    }

    bewerbung.value = data
  } catch (err) {
    console.error('Fehler beim Laden der Bewerbung:', err)
    alert('Bewerbung konnte nicht geladen werden')
    router.replace('/bewerbungen/ApplicationListView')
  }
})

// Zurück zur Übersicht
function goBack() {
  router.push('/bewerbungen/ApplicationListView')
}
</script>

<style scoped>
h1 {
  margin-bottom: 1rem;
}
</style>
