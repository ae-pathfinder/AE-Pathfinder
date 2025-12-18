<template>
  <v-container>
    <v-card outlined class="pa-4">
      <v-card-title>
        <v-icon color="primary" class="mr-2">mdi-view-dashboard</v-icon>
        Dashboard
      </v-card-title>

      <v-card-text class="mt-4">
        <!-- Matching Jobs Section -->
        <MatchingJobText
          :jobs="matchingJobs"
          @view-job="goToJob"
        />

        <v-divider class="my-6" />

        <!-- Application Status Section -->
        <ApplicationStatusText :nwkId="nwkId!" />
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import MatchingJobText from '@/components/textbrackets/MatchingJobText.vue'
import ApplicationStatusText from '@/components/textbrackets/ApplicationStatusText.vue'

const router = useRouter()
const nwkId = ref<number | null>(null) // dynamisch aus Session-Login

interface Job {
  id: number
  titel: string
  beschreibung: string
  standort: string
  matchingScore?: number
}

const jobs = ref<Job[]>([])

// --- Jobs laden und Matching-Score vom Backend abrufen ---
const ladeJobs = async () => {
  if (!nwkId.value) return
  try {
    const response = await axios.get('/api/stellenportal')
    jobs.value = response.data

    // Matching-Score für jede Stelle abrufen
    await Promise.all(
      jobs.value.map(async (job) => {
        try {
          const scoreResp = await axios.get(
            `/api/matching/${nwkId.value}/${job.id}`
          )
          job.matchingScore = Number(scoreResp.data ?? 0)
        } catch {
          job.matchingScore = 0
        }
      })
    )
  } catch (error) {
    console.error('Fehler beim Laden der Jobs:', error)
  }
}

// --- Filter: nur Jobs mit MatchingScore > 30% ---
const matchingJobs = computed(() =>
  jobs.value.filter(job => (job.matchingScore ?? 0) > 50)
)

// --- Navigation zu Jobdetails ---
function goToJob(job: Job) {
  router.push(`/stellen/${job.id}/JobpostingTemplateView`)
}

// onMounted: nwkId aus sessionStorage, dann Jobs laden
onMounted(() => {
  const loggedIn = sessionStorage.getItem('loggedIn') === 'true'
  if (!loggedIn) {
    console.error('Nutzer nicht eingeloggt')
    router.push('/LoginView')
    return
  }

  const userJson = sessionStorage.getItem('user')
  if (!userJson) {
    console.error('Kein eingeloggter Nutzer gefunden')
    router.push('/LoginView')
    return
  }

  const userData = JSON.parse(userJson)
  nwkId.value = userData.id
  ladeJobs()
})
</script>
