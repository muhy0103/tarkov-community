package com.tarkovcommunity.tarkov.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.tarkov.dto.AmmoResponse;
import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.HideoutStationResponse;
import com.tarkovcommunity.tarkov.dto.ItemResponse;
import com.tarkovcommunity.tarkov.dto.QuestResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.dto.WeaponResponse;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.HideoutStation;
import com.tarkovcommunity.tarkov.entity.TarkovAmmo;
import com.tarkovcommunity.tarkov.entity.TarkovItem;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.entity.TarkovQuest;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.entity.TarkovWeapon;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import com.tarkovcommunity.tarkov.service.TarkovCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .map(trader -> new TraderResponse(trader.getId(), trader.getNameEn(), trader.getNameZh(), trader.getUnlockCondition()))
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
                .map(boss -> new BossResponse(boss.getId(), boss.getNameEn(), boss.getNameZh(), boss.getMapId()))
                .toList();
    }

    private static <T> LambdaQueryWrapper<T> enabled(com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, String> statusColumn) {
        return new LambdaQueryWrapper<T>().eq(statusColumn, "ENABLED");
    }
}
