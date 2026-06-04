import request from './request'

const unwrapList = (response) => response?.data ?? []

export const fetchCategories = () => request.get('/categories').then(unwrapList)

export const fetchTags = () => request.get('/tags').then(unwrapList)

export const fetchMaps = () => request.get('/tarkov/maps').then(unwrapList)

export const fetchTraders = () => request.get('/tarkov/traders').then(unwrapList)

export const fetchQuests = () => request.get('/tarkov/quests').then(unwrapList)

export const fetchItems = () => request.get('/tarkov/items').then(unwrapList)

export const fetchWeapons = () => request.get('/tarkov/weapons').then(unwrapList)

export const fetchAmmo = () => request.get('/tarkov/ammo').then(unwrapList)

export const fetchHideoutStations = () =>
  request.get('/tarkov/hideout/stations').then(unwrapList)

export const fetchBosses = () => request.get('/tarkov/bosses').then(unwrapList)

export async function fetchHomeCatalog() {
  const [
    categories,
    maps,
    traders,
    quests,
    items,
    weapons,
    ammo,
    hideoutStations,
    bosses,
  ] = await Promise.all([
    fetchCategories(),
    fetchMaps(),
    fetchTraders(),
    fetchQuests(),
    fetchItems(),
    fetchWeapons(),
    fetchAmmo(),
    fetchHideoutStations(),
    fetchBosses(),
  ])

  return {
    categories,
    maps,
    traders,
    quests,
    items,
    weapons,
    ammo,
    hideoutStations,
    bosses,
  }
}
