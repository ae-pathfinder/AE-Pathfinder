<template>
  <v-container v-if="loggedIn">
    <h1 class="mb-6">Stellenausschreibung</h1>

    <v-container class="box">
      <v-row>
        <!-- JobCard -->
        <v-col cols="12" v-if="stelle">
          <JobCard
            :stelle="stelle"
            @merken="merkeStelle"
          />
        </v-col>

        <v-col v-else cols="12">
          <v-alert type="info">Stelle wird geladen...</v-alert>
        </v-col>

        <!-- Bewerbungsbutton -->
        <v-col cols="12" class="mt-3" v-if="stelle?.status === 'OFFEN'">
          <h2>Deine Bewerbung</h2>
          <p>Haben wir dein Interesse geweckt? Dann freuen wir uns über deine Bewerbung!</p>

          <div class="d-flex align-center">
            <!-- Bereits beworben -->
            <v-btn
              v-if="hatBereitsBeworben(stelle.id)"
              color="grey"
              disabled
              outlined
              class="mr-4"
            >
              Bereits beworben
            </v-btn>

            <!-- Datum der Bewerbung -->
            <span v-if="hatBereitsBeworben(stelle.id)">
              Eingereicht am: {{ formatDate(getEingereichtAm(stelle.id) ?? '') }}
            </span>

            <!-- Bewerbungsbutton, wenn noch nicht beworben -->
            <BaseButtonApplication
              v-else
              @click="openDialog(stelle)"
            >
              Jetzt bewerben
            </BaseButtonApplication>
          </div>
        </v-col>

        <!-- Zurück -->
        <v-col cols="12" class="mt-3">
          <v-btn color="primary" @click="goBack">Zurück zur Übersicht</v-btn>
        </v-col>
      </v-row>
    </v-container>

    <!-- Bewerbungsdialog -->
    <BaseDialogApplication
      v-model="dialogOpen"
      :stelle="selectedStelle"
      :uploadedFiles="nwkDocuments"
      :nwkId="nwk?.id ?? 0"
      @submitted="handleSubmit"
    />
  </v-container>

  <div v-else>
    <p>Bitte einloggen...</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

import JobCard from '@/components/stellen/JobCard.vue'
import BaseButtonApplication from '@/components/common/BaseButtonApplication.vue'
import BaseDialogApplication from '@/components/bewerbungen/BaseDialogApplication.vue'

interface GemerkteStelleDTO {
  stellenId: number
  titel: string
}

const route = useRoute()
const router = useRouter()

const API_STELLE = '/api/stellenportal'
const API_NWK_DOCS = '/api/meinKonto/documents'
const API_BEW = '/api/bewerbungen'
const API_MERKLISTE = '/api/meineListe'

const nwk = ref<{ id: number } | null>(null)
const loggedIn = ref(false)
const stelle = ref<any>(null)
const dialogOpen = ref(false)
const selectedStelle = ref<any>(null)
const nwkDocuments = ref<any[]>([])
const bewerbungs = ref<any[]>([])
const bewerbungsIds = ref<number[]>([])
const gemerkteStellenIds = ref<number[]>([])
const jobId = computed(() => Number((route.params as { id: string }).id))

// -------------------------------------------------------------
// Lade gemerkte Stellen
// -------------------------------------------------------------
const ladeGemerkteStellen = async () => {
  if (!nwk.value) return
  try {
    const res = await axios.get<GemerkteStelleDTO[]>(`${API_MERKLISTE}/nachwuchskraft/${nwk.value.id}`)
    gemerkteStellenIds.value = res.data.map(j => j.stellenId)
  } catch (err) {
    console.error('Fehler beim Laden der Merkliste', err)
    gemerkteStellenIds.value = []
  }
}

// -------------------------------------------------------------
// Lade Stelle
// -------------------------------------------------------------
const ladeStelle = async (id: string | string[]) => {
  if (!id) return
  try {
    const res = await axios.get(`${API_STELLE}/${id}`)
    stelle.value = {
      ...res.data,
      gemerkt: gemerkteStellenIds.value.includes(res.data.id)
    }
  } catch (err) {
    console.error('Fehler beim Laden der Stelle:', err)
  }
}

// -------------------------------------------------------------
// Lade Bewerbungen
// -------------------------------------------------------------
const ladeBewerbungen = async () => {
  if (!nwk.value) return
  try {
    const res = await axios.get(`${API_BEW}/nachwuchskraft/${nwk.value.id}`)
    bewerbungs.value = res.data
    bewerbungsIds.value = res.data.map((b: any) => b.stelleId)
  } catch (err) {
    console.error('Fehler beim Laden der Bewerbungen:', err)
  }
}

// -------------------------------------------------------------
// Merkliste: Hinzufügen / Entfernen
// -------------------------------------------------------------
async function merkeStelle(id: number) {
  if (!nwk.value || !stelle.value) return

  try {
    if (!stelle.value.gemerkt) {
      await axios.post(`${API_MERKLISTE}/${id}/merken/nachwuchskraft/${nwk.value.id}`)
      gemerkteStellenIds.value.push(id)
      stelle.value = { ...stelle.value, gemerkt: true }
      return
    }

    const confirmDelete = confirm("Möchtest du die Stelle von der Merkliste entfernen?")
    if (!confirmDelete) return

    await axios.delete(`${API_MERKLISTE}/${id}/nachwuchskraft/${nwk.value.id}`)
    gemerkteStellenIds.value = gemerkteStellenIds.value.filter(x => x !== id)
    stelle.value = { ...stelle.value, gemerkt: false }
  } catch (err) {
    console.error("Merken/Entfernen fehlgeschlagen:", err)
    alert("Fehler beim Merken/Entfernen")
  }
}

// -------------------------------------------------------------
// Bewerbungsdialog öffnen
// -------------------------------------------------------------
function openDialog(s: any) {
  selectedStelle.value = s
  dialogOpen.value = true
}

// -------------------------------------------------------------
// Hilfsfunktionen Bewerbung
// -------------------------------------------------------------
function hatBereitsBeworben(stelleId: number): boolean {
  return bewerbungsIds.value.includes(stelleId)
}

function getEingereichtAm(stelleId: number): string | null {
  const bew = bewerbungs.value.find((b: any) => b.stelleId === stelleId)
  return bew ? bew.eingereichtAm : null
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('de-DE')
}

// -------------------------------------------------------------
// Zurück
// -------------------------------------------------------------
function goBack() {
  router.push('/stellen/JobpostingOverviewView')
}

// -------------------------------------------------------------
// Mounted
// -------------------------------------------------------------
onMounted(async () => {
  loggedIn.value = sessionStorage.getItem("loggedIn") === "true"
  if (!loggedIn.value) return router.replace("/login")

  const userJson = sessionStorage.getItem("user")
  if (!userJson) return router.replace("/login")
  nwk.value = { id: JSON.parse(userJson).id }

  // Reihenfolge wichtig
  await ladeGemerkteStellen()
  await ladeStelle(jobId.value.toString())
  await ladeBewerbungen()
  await ladeDocuments()
})

const ladeDocuments = async () => {
  if (!nwk.value) return
  try {
    const res = await axios.get(`${API_NWK_DOCS}/${nwk.value.id}`)
    nwkDocuments.value = res.data.documents || []
  } catch (err) {
    console.error("Fehler beim Laden der Dokumente:", err)
  }
}

function handleSubmit() {
  dialogOpen.value = false
  ladeBewerbungen()
}
</script>

<style scoped>
.box { margin-top: 20px; }
</style>
