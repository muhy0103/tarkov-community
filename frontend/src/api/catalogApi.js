import request from './request'

const unwrapList = (response) => response?.data ?? []

export const fetchCategories = () => request.get('/categories').then(unwrapList)

export const fetchTags = () => request.get('/tags').then(unwrapList)

export const fetchAnnouncements = (params = {}) =>
  request
    .get('/announcements', {
      params: {
        page: 1,
        size: 3,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 3, total: 0, pages: 0, records: [] })

export const fetchAnnouncement = (id) =>
  request.get(`/announcements/${id}`).then((response) => response?.data)

export const fetchMaps = () => request.get('/tarkov/maps').then(unwrapList)

export const fetchMapDetail = (id) => request.get(`/tarkov/maps/${id}`).then((response) => response?.data)

export const fetchTraders = () => request.get('/tarkov/traders').then(unwrapList)

export const fetchTraderDetail = (id) =>
  request.get(`/tarkov/traders/${id}`).then((response) => response?.data)

export const fetchQuests = () => request.get('/tarkov/quests').then(unwrapList)

export const fetchQuestDetail = (id) =>
  request.get(`/tarkov/quests/${id}`).then((response) => response?.data)

export const fetchItems = () => request.get('/tarkov/items').then(unwrapList)

export const fetchItemDetail = (id) => request.get(`/tarkov/items/${id}`).then((response) => response?.data)

export const fetchWeapons = () => request.get('/tarkov/weapons').then(unwrapList)

export const fetchWeaponDetail = (id) =>
  request.get(`/tarkov/weapons/${id}`).then((response) => response?.data)

export const fetchAmmo = () => request.get('/tarkov/ammo').then(unwrapList)

export const fetchAmmoDetail = (id) => request.get(`/tarkov/ammo/${id}`).then((response) => response?.data)

export const fetchHideoutStations = () =>
  request.get('/tarkov/hideout/stations').then(unwrapList)

export const fetchHideoutStationDetail = (id) =>
  request.get(`/tarkov/hideout/stations/${id}`).then((response) => response?.data)

export const fetchBosses = () => request.get('/tarkov/bosses').then(unwrapList)

export const fetchBossDetail = (id) => request.get(`/tarkov/bosses/${id}`).then((response) => response?.data)

export const catalogTypeOptions = [
  { label: '地图', type: 'MAP', kind: 'maps', fetcher: fetchMaps },
  { label: '商人', type: 'TRADER', kind: 'traders', fetcher: fetchTraders },
  { label: '任务', type: 'QUEST', kind: 'quests', fetcher: fetchQuests },
  { label: '物品', type: 'ITEM', kind: 'items', fetcher: fetchItems },
  { label: '武器', type: 'WEAPON', kind: 'weapons', fetcher: fetchWeapons },
  { label: '弹药', type: 'AMMO', kind: 'ammo', fetcher: fetchAmmo },
  { label: 'Boss', type: 'BOSS', kind: 'bosses', fetcher: fetchBosses },
  { label: '藏身处', type: 'HIDEOUT', kind: 'hideout', fetcher: fetchHideoutStations },
]

export function routeKindForCatalogType(type) {
  return catalogTypeOptions.find((option) => option.type === type)?.kind || ''
}

export async function fetchCatalogCollections() {
  return Promise.all(catalogTypeOptions.map(async (option) => ({
    ...option,
    items: await option.fetcher(),
  })))
}

export function fetchCatalogDetail(kind, id) {
  const fetchers = {
    maps: fetchMapDetail,
    traders: fetchTraderDetail,
    quests: fetchQuestDetail,
    items: fetchItemDetail,
    weapons: fetchWeaponDetail,
    ammo: fetchAmmoDetail,
    bosses: fetchBossDetail,
    hideout: fetchHideoutStationDetail,
  }

  const fetcher = fetchers[kind]
  if (!fetcher) {
    return Promise.reject(new Error('资料类型不存在'))
  }

  return fetcher(id)
}

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
