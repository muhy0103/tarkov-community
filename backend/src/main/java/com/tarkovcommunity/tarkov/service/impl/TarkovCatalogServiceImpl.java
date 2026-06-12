package com.tarkovcommunity.tarkov.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.tarkovcommunity.tarkov.dto.AmmoDetailResponse;
import com.tarkovcommunity.tarkov.dto.AmmoResponse;
import com.tarkovcommunity.tarkov.dto.BossDetailResponse;
import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.HideoutStationDetailResponse;
import com.tarkovcommunity.tarkov.dto.HideoutStationResponse;
import com.tarkovcommunity.tarkov.dto.HideoutUpgradeResponse;
import com.tarkovcommunity.tarkov.dto.ItemDetailResponse;
import com.tarkovcommunity.tarkov.dto.ItemResponse;
import com.tarkovcommunity.tarkov.dto.MapExtractResponse;
import com.tarkovcommunity.tarkov.dto.MapLootAreaResponse;
import com.tarkovcommunity.tarkov.dto.QuestDetailResponse;
import com.tarkovcommunity.tarkov.dto.QuestResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapDetailResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
import com.tarkovcommunity.tarkov.dto.TraderDetailResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.dto.WeaponDetailResponse;
import com.tarkovcommunity.tarkov.dto.WeaponResponse;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.HideoutStation;
import com.tarkovcommunity.tarkov.entity.HideoutUpgrade;
import com.tarkovcommunity.tarkov.entity.MapExtract;
import com.tarkovcommunity.tarkov.entity.MapLootArea;
import com.tarkovcommunity.tarkov.entity.QuestPrerequisite;
import com.tarkovcommunity.tarkov.entity.TarkovAmmo;
import com.tarkovcommunity.tarkov.entity.TarkovItem;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.entity.TarkovQuest;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.entity.TarkovWeapon;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutUpgradeMapper;
import com.tarkovcommunity.tarkov.mapper.MapExtractMapper;
import com.tarkovcommunity.tarkov.mapper.MapLootAreaMapper;
import com.tarkovcommunity.tarkov.mapper.QuestPrerequisiteMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import com.tarkovcommunity.tarkov.service.TarkovCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarkovCatalogServiceImpl implements TarkovCatalogService {

    private final TarkovMapMapper mapMapper;
    private final TarkovTraderMapper traderMapper;
    private final TarkovQuestMapper questMapper;
    private final TarkovItemMapper itemMapper;
    private final TarkovWeaponMapper weaponMapper;
    private final TarkovAmmoMapper ammoMapper;
    private final HideoutStationMapper hideoutStationMapper;
    private final HideoutUpgradeMapper hideoutUpgradeMapper;
    private final MapExtractMapper mapExtractMapper;
    private final MapLootAreaMapper mapLootAreaMapper;
    private final QuestPrerequisiteMapper questPrerequisiteMapper;
    private final BossMapper bossMapper;

    @Override
    public List<TarkovMapResponse> listMaps() {
        return mapMapper.selectList(enabled(TarkovMap::getStatus).orderByAsc(TarkovMap::getId))
                .stream()
                .map(map -> new TarkovMapResponse(map.getId(), map.getNameEn(), map.getNameZh(), map.getDifficulty(), map.getRecommendedLevel()))
                .toList();
    }

    @Override
    public List<TraderResponse> listTraders() {
        return traderMapper.selectList(enabled(TarkovTrader::getStatus).orderByAsc(TarkovTrader::getId))
                .stream()
                .map(trader -> new TraderResponse(trader.getId(), trader.getNameEn(), null, trader.getUnlockCondition()))
                .toList();
    }

    @Override
    public List<QuestResponse> listQuests() {
        return questMapper.selectList(enabled(TarkovQuest::getStatus).orderByAsc(TarkovQuest::getId))
                .stream()
                .map(quest -> new QuestResponse(quest.getId(), quest.getTraderId(), quest.getNameEn(), quest.getNameZh(), quest.getQuestType(), quest.getMapId()))
                .toList();
    }

    @Override
    public List<ItemResponse> listItems() {
        return itemMapper.selectList(enabled(TarkovItem::getStatus).orderByAsc(TarkovItem::getId))
                .stream()
                .map(item -> new ItemResponse(item.getId(), item.getNameEn(), item.getNameZh(), item.getItemType(), item.getQuestNeeded(), item.getHideoutNeeded(), item.getKeepSuggestion()))
                .toList();
    }

    @Override
    public List<WeaponResponse> listWeapons() {
        return weaponMapper.selectList(enabled(TarkovWeapon::getStatus).orderByAsc(TarkovWeapon::getId))
                .stream()
                .map(weapon -> new WeaponResponse(weapon.getId(), weapon.getNameEn(), weapon.getNameZh(), weapon.getWeaponType(), weapon.getCaliber()))
                .toList();
    }

    @Override
    public List<AmmoResponse> listAmmo() {
        return ammoMapper.selectList(enabled(TarkovAmmo::getStatus).orderByAsc(TarkovAmmo::getId))
                .stream()
                .map(ammo -> new AmmoResponse(ammo.getId(), ammo.getNameEn(), ammo.getNameZh(), ammo.getCaliber(), ammo.getDamage(), ammo.getPenetration()))
                .toList();
    }

    @Override
    public List<HideoutStationResponse> listHideoutStations() {
        return hideoutStationMapper.selectList(enabled(HideoutStation::getStatus).orderByAsc(HideoutStation::getId))
                .stream()
                .map(station -> new HideoutStationResponse(station.getId(), station.getNameEn(), station.getNameZh()))
                .toList();
    }

    @Override
    public List<BossResponse> listBosses() {
        return bossMapper.selectList(enabled(Boss::getStatus).orderByAsc(Boss::getId))
                .stream()
                .map(boss -> new BossResponse(boss.getId(), boss.getNameEn(), null, boss.getMapId()))
                .toList();
    }

    @Override
    public TarkovMapDetailResponse getMapDetail(Long id) {
        TarkovMap map = requireEnabled(mapMapper, id, TarkovMap::getId, TarkovMap::getStatus, "地图资料不存在或未启用");
        List<MapExtractResponse> extracts = mapExtractMapper.selectList(new LambdaQueryWrapper<MapExtract>()
                        .eq(MapExtract::getMapId, id)
                        .eq(MapExtract::getStatus, "ENABLED")
                        .orderByAsc(MapExtract::getId))
                .stream()
                .map(this::toExtractResponse)
                .toList();
        List<MapLootAreaResponse> lootAreas = mapLootAreaMapper.selectList(new LambdaQueryWrapper<MapLootArea>()
                        .eq(MapLootArea::getMapId, id)
                        .orderByAsc(MapLootArea::getId))
                .stream()
                .map(this::toLootAreaResponse)
                .toList();
        List<BossResponse> bosses = bossMapper.selectList(enabled(Boss::getStatus)
                        .eq(Boss::getMapId, id)
                        .orderByAsc(Boss::getId))
                .stream()
                .map(this::toBossResponse)
                .toList();
        List<QuestResponse> quests = questMapper.selectList(enabled(TarkovQuest::getStatus)
                        .eq(TarkovQuest::getMapId, id)
                        .orderByAsc(TarkovQuest::getId))
                .stream()
                .map(this::toQuestResponse)
                .toList();

        return new TarkovMapDetailResponse(
                map.getId(),
                map.getNameEn(),
                map.getNameZh(),
                map.getDifficulty(),
                map.getDescription(),
                map.getRecommendedLevel(),
                extracts,
                lootAreas,
                bosses,
                quests
        );
    }

    @Override
    public TraderDetailResponse getTraderDetail(Long id) {
        TarkovTrader trader = requireEnabled(traderMapper, id, TarkovTrader::getId, TarkovTrader::getStatus, "商人资料不存在或未启用");
        List<QuestResponse> quests = questMapper.selectList(enabled(TarkovQuest::getStatus)
                        .eq(TarkovQuest::getTraderId, id)
                        .orderByAsc(TarkovQuest::getId))
                .stream()
                .map(this::toQuestResponse)
                .toList();

        return new TraderDetailResponse(
                trader.getId(),
                trader.getNameEn(),
                null,
                trader.getDescription(),
                trader.getUnlockCondition(),
                trader.getAvatar(),
                quests
        );
    }

    @Override
    public QuestDetailResponse getQuestDetail(Long id) {
        TarkovQuest quest = requireEnabled(questMapper, id, TarkovQuest::getId, TarkovQuest::getStatus, "任务资料不存在或未启用");
        TraderResponse trader = quest.getTraderId() == null ? null : nullableTrader(quest.getTraderId());
        TarkovMapResponse map = quest.getMapId() == null ? null : nullableMap(quest.getMapId());
        List<Long> prerequisiteIds = questPrerequisiteMapper.selectList(new LambdaQueryWrapper<QuestPrerequisite>()
                        .eq(QuestPrerequisite::getQuestId, id))
                .stream()
                .map(QuestPrerequisite::getPrerequisiteQuestId)
                .toList();
        List<QuestResponse> prerequisites = prerequisiteIds.isEmpty()
                ? List.of()
                : questMapper.selectList(enabled(TarkovQuest::getStatus)
                        .in(TarkovQuest::getId, prerequisiteIds)
                        .orderByAsc(TarkovQuest::getId))
                .stream()
                .map(this::toQuestResponse)
                .toList();

        return new QuestDetailResponse(
                quest.getId(),
                quest.getTraderId(),
                quest.getNameEn(),
                quest.getNameZh(),
                quest.getQuestType(),
                quest.getMapId(),
                quest.getDescription(),
                quest.getRewards(),
                quest.getUnlocks(),
                trader,
                map,
                prerequisites
        );
    }

    @Override
    public ItemDetailResponse getItemDetail(Long id) {
        TarkovItem item = requireEnabled(itemMapper, id, TarkovItem::getId, TarkovItem::getStatus, "物品资料不存在或未启用");
        return new ItemDetailResponse(
                item.getId(),
                item.getNameEn(),
                item.getNameZh(),
                item.getItemType(),
                item.getRarity(),
                item.getGridSize(),
                item.getQuestNeeded(),
                item.getHideoutNeeded(),
                item.getKeepSuggestion(),
                item.getDescription()
        );
    }

    @Override
    public WeaponDetailResponse getWeaponDetail(Long id) {
        TarkovWeapon weapon = requireEnabled(weaponMapper, id, TarkovWeapon::getId, TarkovWeapon::getStatus, "武器资料不存在或未启用");
        List<AmmoResponse> compatibleAmmo = ammoMapper.selectList(enabled(TarkovAmmo::getStatus)
                        .eq(TarkovAmmo::getCaliber, weapon.getCaliber())
                        .orderByDesc(TarkovAmmo::getPenetration)
                        .orderByDesc(TarkovAmmo::getDamage))
                .stream()
                .map(this::toAmmoResponse)
                .toList();

        return new WeaponDetailResponse(
                weapon.getId(),
                weapon.getNameEn(),
                weapon.getNameZh(),
                weapon.getWeaponType(),
                weapon.getCaliber(),
                weapon.getDescription(),
                compatibleAmmo
        );
    }

    @Override
    public AmmoDetailResponse getAmmoDetail(Long id) {
        TarkovAmmo ammo = requireEnabled(ammoMapper, id, TarkovAmmo::getId, TarkovAmmo::getStatus, "弹药资料不存在或未启用");
        List<WeaponResponse> compatibleWeapons = weaponMapper.selectList(enabled(TarkovWeapon::getStatus)
                        .eq(TarkovWeapon::getCaliber, ammo.getCaliber())
                        .orderByAsc(TarkovWeapon::getId))
                .stream()
                .map(this::toWeaponResponse)
                .toList();

        return new AmmoDetailResponse(
                ammo.getId(),
                ammo.getNameEn(),
                ammo.getNameZh(),
                ammo.getCaliber(),
                ammo.getDamage(),
                ammo.getPenetration(),
                ammo.getArmorDamage(),
                ammo.getDescription(),
                compatibleWeapons
        );
    }

    @Override
    public HideoutStationDetailResponse getHideoutStationDetail(Long id) {
        HideoutStation station = requireEnabled(hideoutStationMapper, id, HideoutStation::getId, HideoutStation::getStatus, "藏身处设施不存在或未启用");
        List<HideoutUpgradeResponse> upgrades = hideoutUpgradeMapper.selectList(new LambdaQueryWrapper<HideoutUpgrade>()
                        .eq(HideoutUpgrade::getStationId, id)
                        .orderByAsc(HideoutUpgrade::getLevel))
                .stream()
                .map(this::toHideoutUpgradeResponse)
                .toList();

        return new HideoutStationDetailResponse(
                station.getId(),
                station.getNameEn(),
                station.getNameZh(),
                station.getDescription(),
                upgrades
        );
    }

    @Override
    public BossDetailResponse getBossDetail(Long id) {
        Boss boss = requireEnabled(bossMapper, id, Boss::getId, Boss::getStatus, "Boss 资料不存在或未启用");
        TarkovMapResponse map = boss.getMapId() == null ? null : nullableMap(boss.getMapId());

        return new BossDetailResponse(
                boss.getId(),
                boss.getNameEn(),
                null,
                boss.getMapId(),
                boss.getDescription(),
                boss.getEquipmentSummary(),
                map
        );
    }

    private static <T> LambdaQueryWrapper<T> enabled(com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, String> statusColumn) {
        return new LambdaQueryWrapper<T>().eq(statusColumn, "ENABLED");
    }

    private <T> T requireEnabled(
            BaseMapper<T> mapper,
            Long id,
            SFunction<T, Long> idColumn,
            SFunction<T, String> statusColumn,
            String message
    ) {
        T entity = mapper.selectOne(new LambdaQueryWrapper<T>()
                .eq(idColumn, id)
                .eq(statusColumn, "ENABLED"));
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        return entity;
    }

    private TarkovMapResponse nullableMap(Long id) {
        TarkovMap map = mapMapper.selectOne(enabled(TarkovMap::getStatus).eq(TarkovMap::getId, id));
        return map == null ? null : toMapResponse(map);
    }

    private TraderResponse nullableTrader(Long id) {
        TarkovTrader trader = traderMapper.selectOne(enabled(TarkovTrader::getStatus).eq(TarkovTrader::getId, id));
        return trader == null ? null : toTraderResponse(trader);
    }

    private TarkovMapResponse toMapResponse(TarkovMap map) {
        return new TarkovMapResponse(map.getId(), map.getNameEn(), map.getNameZh(), map.getDifficulty(), map.getRecommendedLevel());
    }

    private TraderResponse toTraderResponse(TarkovTrader trader) {
        return new TraderResponse(trader.getId(), trader.getNameEn(), null, trader.getUnlockCondition());
    }

    private QuestResponse toQuestResponse(TarkovQuest quest) {
        return new QuestResponse(quest.getId(), quest.getTraderId(), quest.getNameEn(), quest.getNameZh(), quest.getQuestType(), quest.getMapId());
    }

    private WeaponResponse toWeaponResponse(TarkovWeapon weapon) {
        return new WeaponResponse(weapon.getId(), weapon.getNameEn(), weapon.getNameZh(), weapon.getWeaponType(), weapon.getCaliber());
    }

    private AmmoResponse toAmmoResponse(TarkovAmmo ammo) {
        return new AmmoResponse(ammo.getId(), ammo.getNameEn(), ammo.getNameZh(), ammo.getCaliber(), ammo.getDamage(), ammo.getPenetration());
    }

    private BossResponse toBossResponse(Boss boss) {
        return new BossResponse(boss.getId(), boss.getNameEn(), null, boss.getMapId());
    }

    private MapExtractResponse toExtractResponse(MapExtract extract) {
        return new MapExtractResponse(extract.getId(), extract.getName(), extract.getFactionLimit(), extract.getConditionText(), extract.getDescription());
    }

    private MapLootAreaResponse toLootAreaResponse(MapLootArea area) {
        return new MapLootAreaResponse(area.getId(), area.getName(), area.getLootType(), area.getRiskLevel(), area.getDescription());
    }

    private HideoutUpgradeResponse toHideoutUpgradeResponse(HideoutUpgrade upgrade) {
        return new HideoutUpgradeResponse(upgrade.getId(), upgrade.getLevel(), upgrade.getRequiredItems(), upgrade.getRequiredTime(), upgrade.getUnlocks());
    }
}
