package com.tarkovcommunity.tarkov.controller;

import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.tarkov.dto.AmmoDetailResponse;
import com.tarkovcommunity.tarkov.dto.AmmoResponse;
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
import com.tarkovcommunity.tarkov.service.TarkovCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tarkov")
@RequiredArgsConstructor
public class TarkovCatalogController {

    private final TarkovCatalogService tarkovCatalogService;

    @GetMapping("/maps")
    public ApiResponse<List<TarkovMapResponse>> listMaps() {
        return ApiResponse.success(tarkovCatalogService.listMaps());
    }

    @GetMapping("/maps/{id}")
    public ApiResponse<TarkovMapDetailResponse> getMap(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getMapDetail(id));
    }

    @GetMapping("/traders")
    public ApiResponse<List<TraderResponse>> listTraders() {
        return ApiResponse.success(tarkovCatalogService.listTraders());
    }

    @GetMapping("/traders/{id}")
    public ApiResponse<TraderDetailResponse> getTrader(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getTraderDetail(id));
    }

    @GetMapping("/quests")
    public ApiResponse<List<QuestResponse>> listQuests() {
        return ApiResponse.success(tarkovCatalogService.listQuests());
    }

    @GetMapping("/quests/{id}")
    public ApiResponse<QuestDetailResponse> getQuest(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getQuestDetail(id));
    }

    @GetMapping("/items")
    public ApiResponse<List<ItemResponse>> listItems() {
        return ApiResponse.success(tarkovCatalogService.listItems());
    }

    @GetMapping("/items/{id}")
    public ApiResponse<ItemDetailResponse> getItem(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getItemDetail(id));
    }

    @GetMapping("/weapons")
    public ApiResponse<List<WeaponResponse>> listWeapons() {
        return ApiResponse.success(tarkovCatalogService.listWeapons());
    }

    @GetMapping("/weapons/{id}")
    public ApiResponse<WeaponDetailResponse> getWeapon(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getWeaponDetail(id));
    }

    @GetMapping("/ammo")
    public ApiResponse<List<AmmoResponse>> listAmmo() {
        return ApiResponse.success(tarkovCatalogService.listAmmo());
    }

    @GetMapping("/ammo/{id}")
    public ApiResponse<AmmoDetailResponse> getAmmo(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getAmmoDetail(id));
    }

    @GetMapping("/hideout/stations")
    public ApiResponse<List<HideoutStationResponse>> listHideoutStations() {
        return ApiResponse.success(tarkovCatalogService.listHideoutStations());
    }

    @GetMapping("/hideout/stations/{id}")
    public ApiResponse<HideoutStationDetailResponse> getHideoutStation(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getHideoutStationDetail(id));
    }

    @GetMapping("/bosses")
    public ApiResponse<List<BossResponse>> listBosses() {
        return ApiResponse.success(tarkovCatalogService.listBosses());
    }

    @GetMapping("/bosses/{id}")
    public ApiResponse<BossDetailResponse> getBoss(@PathVariable Long id) {
        return ApiResponse.success(tarkovCatalogService.getBossDetail(id));
    }
}
