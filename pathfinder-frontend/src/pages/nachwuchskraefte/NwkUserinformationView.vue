<template>
  <v-container>
    <h1 class="mb-6">Mein Konto</h1>

    <!-- Persönliche Daten -->
    <BaseCardNwkPersonal
      v-if="nwk"
      :nwk="nwk"
      editable
      class="mt-4"
      @edit="dialogProfileOpen = true"
    />
    <v-card v-else class="mt-4 pa-4" outlined>
      <v-row justify="center">
        <v-progress-circular indeterminate color="primary" />
      </v-row>
      <v-row justify="center" class="mt-2">
        <span>Lade persönliche Daten...</span>
      </v-row>
    </v-card>
    <!-- Abteilungen & Interessen -->
    <BaseCardNwkExperienceAndInterests
      v-if="nwkExperience && optionsLoaded"
      :nwkExperience="{
          ...nwkExperience,
          programmieren: nwkExperience?.programmieren ?? false,
          programmiersprachen: nwkExperience?.programmiersprachen ?? null
        }"
      :options="{ tags, abteilungen }"
      editable
      class="mt-4"
      @edit="dialogExperienceOpen = true"
    />
    <!-- Dokumente -->
    <BaseCardNwkDocuments
      v-if="nwk"
      :savedFiles="savedFiles"
      :nwkId="nwk.id"
      editable
      @upload="dialogOpen = true"
      @delete="handleDeleteFile"
    />

    <!-- Dialoge -->
    <BaseDialogNwkUploadDocuments
      v-model="dialogOpen"
      :savedFiles="savedFiles"
      :nwkId="nwk?.id ?? 0"
      @save="handleUploadSave"
    />

    <BaseDialogNwkUpdateExperienceAndInterests
      v-model="dialogExperienceOpen"
      :nwkExperience="nwkExperience"
      :nwkId="nwk?.id ?? 0"
      :options="{ tags, abteilungen }"
      @save="handleExperienceSave"
    />
  </v-container>
  <v-btn
      v-show="showScrollTop"
      class="scroll-top-btn"
      icon="mdi-arrow-up"
      color="primary"
      @click="scrollToTop"
    />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import BaseDialogNwkUploadDocuments from '@/components/nachwuchskraefte/BaseDialogNwkUploadDocuments.vue'
import BaseCardNwkPersonal from '@/components/nachwuchskraefte/BaseCardNwkPersonal.vue'
import BaseCardNwkDocuments from '@/components/nachwuchskraefte/BaseCardNwkDocuments.vue'
import BaseCardNwkExperienceAndInterests from '@/components/nachwuchskraefte/BaseCardNwkExperienceAndInterests.vue'
import BaseDialogNwkUpdateExperienceAndInterests from '@/components/nachwuchskraefte/BaseDialogNwkUpdateExperienceAndInterests.vue'

interface Nachwuchskraft {
  id: number
  personalnummer: string
  vorname: string
  nachname: string
  email: string
  studienrichtung: string
  jahrgang: string
  praktika: { id: number; name: string }[]
}

interface NwkExperience {
  wunschabteilungen: { id: number; name: string }[]
  interessen: { id: number; name: string }[]
  programmieren?: boolean
  programmiersprachen?: string | null
}

interface StoredFile {
  id: number
  name: string
  url: string
  fileObject?: File
}

interface OptionData {
  tags: { id: number; name: string }[]
  abteilungen: { id: number; name: string }[]
}

// ---------------- State ----------------
const router = useRouter()
const loggedIn = ref(false)
const nwk = ref<Nachwuchskraft | null>(null)
const nwkExperience = ref<NwkExperience | null>(null)
const savedFiles = ref<StoredFile[]>([])
const tags = ref<OptionData['tags']>([])
const abteilungen = ref<OptionData['abteilungen']>([])
const optionsLoaded = ref(false)

const dialogOpen = ref(false)
const dialogProfileOpen = ref(false)
const dialogExperienceOpen = ref(false)

// ---------------- Laden ----------------
async function loadOptions() {
  try {
    const res = await fetch('/api/meinKonto/options')
    if (!res.ok) throw new Error(`Fehler beim Laden der Optionen: ${res.status}`)
    const data = await res.json()
    tags.value = data.tags
    abteilungen.value = data.abteilungen
    optionsLoaded.value = true
  } catch (err) {
    console.error(err)
  }
}

async function loadExperience() {
  if (!nwk.value) return
  try {
    const res = await fetch(`/api/meinKonto/experience/${nwk.value.id}`)
    if (!res.ok) throw new Error(`Fehler beim Laden: ${res.status}`)
    const data = await res.json()
    nwkExperience.value = {
      wunschabteilungen: data.wunschabteilungen,
      interessen: data.interessen,
      programmieren: data.programmieren,
      programmiersprachen: data.programmiersprachen ?? ''
    }
  } catch (err) {
    console.error(err)
  }
}

async function loadPersonal() {
  if (!nwk.value) return

  try {
    const res = await fetch(`/api/meinKonto/personal/${nwk.value.id}`)
    if (!res.ok) throw new Error(`Fehler: ${res.status}`)
    const data = await res.json()
    nwk.value = {
      id: data.id,
      personalnummer: data.personalnummer,
      vorname: data.vorname,
      nachname: data.nachname,
      email: data.email,
      studienrichtung: data.studienrichtung,
      jahrgang: data.jahrgang,
      praktika: data.praktika ?? []
    }
  } catch (err) {
    console.error(err)
  }
}

// ----------------- Dokumente -----------------
async function loadDocuments() {
  if (!nwk.value) return
  try {
    const res = await fetch(`/api/meinKonto/documents/${nwk.value.id}`)
    if (res.status === 204) {
      savedFiles.value = []
      return
    }
    const data = await res.json()
    savedFiles.value = data.map((d: any) => ({
      id: d.id,
      name: d.dateipfad.split('/').pop() ?? 'Unbekannt',
      url: d.dateipfad
    }))
  } catch (err) {
    console.error(err)
  }
}

function handleDeleteFile(fileId: number) {
  console.log('Datei löschen:', fileId)
  // Hier ggf. API-Call zum Löschen implementieren
}

function handleUploadSave(savedFiles: any[]) {
  console.log('Dateien gespeichert:', savedFiles)
  // Hier ggf. State oder API-Call aktualisieren
}


// ---------------- Aktionen ----------------
async function handleExperienceSave(updated: NwkExperience) {
  if (!nwk.value) return
  try {
    const payload = {
      interessenIds: updated.interessen.map(t => t.id),
      wunschabteilungenIds: updated.wunschabteilungen.map(d => d.id),
      programmieren: updated.programmieren,
      programmiersprachen: updated.programmiersprachen ?? ''
    }

    const res = await fetch(`/api/meinKonto/experience/${nwk.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) throw new Error(`Fehler beim Speichern: ${res.status}`)

    // Backend liefert die aktualisierte Experience zurück
    const saved = await res.json()
    nwkExperience.value = {
      wunschabteilungen: saved.wunschabteilungen,
      interessen: saved.interessen,
      programmieren: saved.programmieren,
      programmiersprachen: saved.programmiersprachen ?? ''
    }

    alert('Bevorzugte Abteilungen & Interessen gespeichert!')
  } catch (err) {
    console.error(err)
    alert('Fehler beim Speichern!')
  }
}


// ---------------- Mounted ----------------
onMounted(() => {
  loggedIn.value = sessionStorage.getItem('loggedIn') === 'true'
  if (!loggedIn.value) {
    router.replace('/login')
    return
  }

  const userJson = sessionStorage.getItem('user')
  if (!userJson) return console.error('Kein eingeloggter Nutzer gefunden')
  const userData = JSON.parse(userJson)
  nwk.value = {
    id: userData.id,
    personalnummer: userData.personalnummer,
    vorname: userData.vorname,
    nachname: userData.nachname,
    email: userData.email,
    studienrichtung: userData.studienrichtung,
    jahrgang: userData.jahrgang,
    praktika: userData.praktika ?? []
  }

  loadOptions()
  loadPersonal()
  loadExperience()
  loadDocuments()
})

const showScrollTop = ref(false)

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: "smooth"
  })
}

onMounted(() => {
  window.addEventListener("scroll", () => {
    showScrollTop.value = window.scrollY > 300
  })
})

</script>

<style scoped>
.v-card-title { font-weight: 600; }
.scroll-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 999;
  border-radius: 50%;
}

</style>
