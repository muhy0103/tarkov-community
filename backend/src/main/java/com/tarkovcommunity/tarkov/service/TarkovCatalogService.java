package com.tarkovcommunity.tarkov.service;

import com.tarkovcommunity.tarkov.dto.AmmoResponse;
import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.HideoutStationResponse;
import com.tarkovcommunity.tarkov.dto.ItemResponse;
import com.tarkovcommunity.tarkov.dto.QuestResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
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
}
