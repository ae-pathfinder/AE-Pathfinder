<template>
  <v-btn
    v-show="visible"
    icon
    color="primary"
    class="scroll-top-btn"
    @click="scrollToTop"
  >
    <v-icon>mdi-arrow-up</v-icon>
  </v-btn>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const visible = ref(false)

// Scroll-Funktion
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Scroll-Listener
const handleScroll = () => {
  visible.value = window.scrollY > 300
}

onMounted(() => window.addEventListener('scroll', handleScroll))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
.scroll-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 999;
  border-radius: 50%;
}
</style>
