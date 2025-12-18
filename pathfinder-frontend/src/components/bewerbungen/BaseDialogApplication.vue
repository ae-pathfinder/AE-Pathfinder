<template>
  <v-dialog v-model="dialog" max-width="600px" persistent>
    <v-card>
      <v-card-title class="text-h6 font-weight-bold">
        Bewerbung für: {{ stelle?.titel || stelle?.title || "Unbekannte Stelle" }}
      </v-card-title>

      <v-divider></v-divider>

      <v-card-text>
        <!-- Einwilligung -->
        <v-checkbox
          v-model="consent"
          label="Ich willige ein, dass meine persönlichen Daten für diese Bewerbung weitergegeben werden."
        />

        <!-- Dokumente Dropdown -->
        <v-select
          v-if="uploadedFiles?.length"
          v-model="selectedFilesForApplication"
          :items="uploadedFiles.map(f => ({ id: f.id, name: f.dateipfad.split('/').pop() || f.typ }))"
          item-title="name"
          item-value="id"
          multiple
          chips
          dense
          class="mt-4"
          placeholder="Dokumente auswählen"
        />

        <!-- Info falls keine Dokumente vorhanden -->
        <v-alert v-else type="info" dense class="mt-4">
          Es sind keine hochgeladenen Dokumente vorhanden. Bitte lade zuerst Dokumente hoch.
        </v-alert>

        <!-- Optional: HR-Notiz -->
        <v-textarea
          v-model="hrNote"
          label="Notiz für HR (optional)"
          placeholder="Zusätzliche Informationen oder Wünsche"
          rows="3"
          auto-grow
          class="mt-4"
        />
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions class="justify-end">
        <v-btn text @click="closeDialog">Abbrechen</v-btn>
        <v-btn
          color="primary"
          :disabled="!consent || selectedFilesForApplication.length === 0"
          @click="submitApplication"
        >
          Bewerben
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

interface FileItem {
  id: number;
  dateipfad: string;
  typ: string;
  hochgeladenAm: string;
}

interface Stelle {
  id?: number;
  titel?: string;  // korrekt für deine Stellen-Detail-Seite
  title?: string;  // fallback, falls API english liefert
}

const props = defineProps<{
  modelValue: boolean;
  stelle: Stelle | null;
  uploadedFiles: FileItem[];
  nwkId: any;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
  (e: "submitted"): void;
}>();

const dialog = ref(props.modelValue);
const consent = ref(false);
const selectedFilesForApplication = ref<number[]>([]);
const hrNote = ref("");

// Sync mit v-model
watch(() => props.modelValue, (val) => (dialog.value = val));
watch(dialog, (val) => emit("update:modelValue", val));

// Formular zurücksetzen
function closeDialog() {
  consent.value = false;
  selectedFilesForApplication.value = [];
  hrNote.value = "";
  dialog.value = false;
}

// Bewerbung absenden
async function submitApplication() {
  if (!props.stelle?.id) return alert("Keine gültige Stelle ausgewählt!");

  try {
    const payload = {
      stelleId: props.stelle.id,
      nachwuchskraftId: props.nwkId,
      fileIds: selectedFilesForApplication.value,
      hrNote: hrNote.value || null
    };

    const res = await fetch("/api/bewerbungen", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!res.ok) throw new Error("Fehler beim Bewerben");

    alert("Bewerbung erfolgreich erstellt!");
    closeDialog();
    emit("submitted");

  } catch (err) {
    console.error(err);
    alert("Fehler beim Bewerben!");
  }
}
</script>

<style scoped>
.mt-4 {
  margin-top: 16px;
}
</style>
