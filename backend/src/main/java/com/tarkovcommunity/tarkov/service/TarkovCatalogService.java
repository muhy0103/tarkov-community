package com.tarkovcommunity.tarkov.service;

import com.tarkovcommunity.tarkov.dto.AmmoResponse;
import com.tarkovcommunity.tarkov.dto.AmmoDetailResponse;
import com.tarkovcommunity.tarkov.dto.BossDetailResponse;
import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.HideoutStationDetailResponse;
import com.tarkovcommunity.tarkov.dto.HideoutStationResponse;
import com.tarkovcommunity.tarkov.dto.ItemDetailResponse;
import com.tarkovcommunity.tarkov.dto.ItemResponse;
import com.tarkovcommunity.tarkov.dto.QuestDetailResponse;
import com.tarkovcommunity.tarkov.dto.QuestResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapDetailResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
import com.tarkovcommunity.tarkov.dto.TraderDetailResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.dto.WeaponDetailResponse;
import com.tarkovcommunity.tarkov.dto.WeaponResponse;

import java.util.List;

public interface TarkovCatalogService {
    List<TarkovMapResponse> listMaps();

    List<TraderResponse> listTraders();

    List<QuestResponse> listQuests();

    List<ItemResponse> listItems();

    List<WeaponResponse> listWeapons();

    List<AmmoResponse> listAmmo();

    List<HideoutStationResponse> listHideoutStations();

    List<BossResponse> listBosses();

    TarkovMapDetailResponse getMapDetail(Long id);

    TraderDetailResponse getTraderDetail(Long id);

    QuestDetailResponse getQuestDetail(Long id);

    ItemDetailResponse getItemDetail(Long id);

    WeaponDetailResponse getWeaponDetail(Long id);

    AmmoDetailResponse getAmmoDetail(Long id);

    HideoutStationDetailResponse getHideoutStationDetail(Long id);

    BossDetailResponse getBossDetail(Long id);
}
