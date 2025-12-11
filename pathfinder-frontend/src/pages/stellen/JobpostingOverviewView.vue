<template>
  <v-container v-if="loggedIn">
    <h1 class="mb-6">Offene Stellen</h1>

    <v-text-field
      v-model="search"
      label="Suche"
      prepend-inner-icon="mdi-magnify"
      variant="outlined"
      hide-details
      single-line
    />

    <v-container class="box">
      <v-row>
        <v-col
          v-for="stelle in filteredStellen"
          :key="stelle.id"
          cols="12"
        >
          <router-link
            :to="`/stellen/${stelle.id}/JobpostingTemplateView`"
            class="no-underline"
          >
            <BaseCardJobMini
              class="card-hover"
              :job="stelle"
            />
          </router-link>
        </v-col>
      </v-row>
    </v-container>
  </v-container>

  <div v-else>
    <p>Bitte einloggen...</p>
  </div>

  <v-btn
    v-show="showScrollTop"
    class="scroll-top-btn"
    icon="mdi-arrow-up"
    color="primary"
    @click="scrollToTop"
  />

</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import BaseCardJobMini from '@/components/stellen/BaseCardJobMini.vue'

const router = useRouter()
const search = ref("")
const stellen = ref<any[]>([])
const profileId = ref<number | null>(null)
const loggedIn = ref(false)
const bewerbungen = ref<any[]>([])
const API_URL = '/api/stellenportal'

// Prüfen, ob Nutzer eingeloggt
onMounted(async () => {
  loggedIn.value = sessionStorage.getItem('loggedIn') === 'true'
  if (!loggedIn.value) return router.replace('/login')

  const userJson = sessionStorage.getItem("user")
  if (!userJson) return
  profileId.value = JSON.parse(userJson).id

  await ladeStellen()
  await ladeBewerbungen()
})

// Lade alle Stellen + MatchingScore
const ladeStellen = async () => {
  if (!profileId.value) return
  try {
    const res = await axios.get(API_URL)
    stellen.value = res.data

    for (const stelle of stellen.value) {
      await ladeMatchingScore(stelle)
    }
  } catch (err) {
    console.error("Fehler beim Laden der Stellen:", err)
  }
}

// MatchingScore
const ladeMatchingScore = async (stelle: any) => {
  if (!profileId.value) return
  try {
    const res = await axios.get(`/api/matching/${profileId.value}/${stelle.id}`)
    stelle.matchingScore = res.data
  } catch (err) {
    stelle.matchingScore = 0
  }
}

// Lade Bewerbungen einmal
const ladeBewerbungen = async () => {
  if (!profileId.value) return
  try {
    const res = await axios.get(`/api/bewerbungen/nachwuchskraft/${profileId.value}`)
    bewerbungen.value = res.data

    // Für jede Stelle prüfen, ob bereits beworben
    for (const stelle of stellen.value) {
      stelle.beworben = bewerbungen.value.some((b: any) => b.stelleId === stelle.id)
    }
  } catch (err) {
    console.error("Fehler beim Laden der Bewerbungen:", err)
    for (const stelle of stellen.value) {
      stelle.beworben = false
    }
  }
}


// Filter + Sortierung
const filteredStellen = computed(() => {
  return stellen.value
    .filter(s => {
      const q = search.value.toLowerCase()
      return (s.titel?.toLowerCase().includes(q) || s.beschreibung?.toLowerCase().includes(q))
    })
    .sort((a, b) => {
      if (a.beworben && !b.beworben) return 1
      if (!a.beworben && b.beworben) return -1
      return (b.matchingScore ?? 0) - (a.matchingScore ?? 0)
    })
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
.box {
  margin: 2% 1% 1%;
  border: 2px solid #0000001a;
}
.no-underline {
  text-decoration: none;
  color: inherit;
}
.card-hover {
  transition: transform 0.2s, box-shadow 0.2s;
}
.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  cursor: pointer;
}
.scroll-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 999;
  border-radius: 50%;
}

</style>
