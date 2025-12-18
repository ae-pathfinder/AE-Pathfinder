<template>
  <v-container>
    <h1 class="mb-6">Meine Bewerbungen</h1>

    <v-container class="box">
      <v-row>
        <v-col v-for="bewerbung in bewerbungen" :key="bewerbung.id" cols="12">
          <BaseCardApplicationMini
            class="card-hover"
            :bewerbung="bewerbung"
            @detail="goToDetail"
            @withdraw="handleWithdraw"
          />
        </v-col>

        <!-- Info, falls keine Bewerbungen -->
        <v-col v-if="bewerbungen.length === 0" cols="12">
          <p>Keine Bewerbungen für diese Nachwuchskraft gefunden.</p>
        </v-col>
      </v-row>
    </v-container>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import BaseCardApplicationMini from '@/components/bewerbungen/BaseCardApplicationMini.vue'

interface Stelle {
  id: number
  titel: string
  standort: string
  bewerbungsfrist?: string
}

interface Bewerbung {
  id: number
  status: 'EINGEREICHT' | 'IN_PRUEFUNG' | 'ABGELEHNT' | 'ANGELADEN' | 'ANGENOMMEN'
  eingereichtAm: string
  nachwuchskraftId: number
  stelleId: number
  stelle?: Stelle
  stelleTitel: string
}

const router = useRouter()
const nwkId = ref<number>(0)
const bewerbungen = ref<Bewerbung[]>([])
const API_BEW = '/api/bewerbungen'

// Bewerbungen laden
const ladeBewerbungen = async (id: number) => {
  try {
    const res = await axios.get<Bewerbung[]>(`${API_BEW}/nachwuchskraft/${id}`)
    bewerbungen.value = res.data
  } catch (err) {
    console.error('Fehler beim Laden der Bewerbungen:', err)
    alert('Fehler beim Laden der Bewerbungen')
  }
}

// Weiterleitung zur Detailansicht
const goToDetail = (id: number) => {
  router.push(`/bewerbungen/${id}/ApplicationView`)
}

// Bewerbung zurückziehen
const handleWithdraw = async (id: number) => {
  try {
    await axios.delete(`${API_BEW}/${id}`)
    alert("Bewerbung erfolgreich zurückgezogen")
    bewerbungen.value = bewerbungen.value.filter(b => b.id !== id)
  } catch (err) {
    console.error('Fehler beim Zurückziehen der Bewerbung:', err)
    alert("Fehler beim Zurückziehen der Bewerbung")
  }
}

// onMounted: Nachwuchskraft-ID aus SessionStorage laden und Bewerbungen laden
onMounted(() => {
  const userJson = sessionStorage.getItem('user')
  if (!userJson) {
    console.error('Kein eingeloggter Nutzer gefunden')
    return
  }

  const userData = JSON.parse(userJson)
  nwkId.value = userData.id
  ladeBewerbungen(userData.id)
})
</script>

<style scoped>
.box {
  margin: 2% 1% 1%;
  border: 2px solid #0000001a;
  padding: 16px;
  border-radius: 8px;
  background-color: #ffffff;
}

.card-hover {
  transition: transform 0.2s, box-shadow 0.2s;
}

.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}

h1 {
  color: #000;
  font-weight: 600;
  margin-bottom: 1.5rem;
}
</style>
