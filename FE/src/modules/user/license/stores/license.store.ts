import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LicenseItem } from '@/modules/user/license/models/license.response.ts'

export const useLicenseStore = defineStore('license', () => {
  const items = ref<LicenseItem[]>([])

  function setItems(list: LicenseItem[]) {
    list.forEach((item) => {
      const idx = items.value.findIndex((i) => i.id === item.id)
      if (idx === -1) {
        items.value.push(item)
      } else {
        items.value[idx] = item
      }
    })
  }

  function setItem(item: LicenseItem) {
    const idx = items.value.findIndex((i) => i.id === item.id)
    if (idx === -1) {
      items.value.push(item)
    } else {
      items.value[idx] = item
    }
  }

  function findById(id: string): LicenseItem | undefined {
    return items.value.find((i) => i.id === id)
  }

  return { items, setItems, setItem, findById }
})