package com.tarkovcommunity.tarkov.controller;

import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.tarkov.dto.AmmoResponse;
import com.tarkovcommunity.tarkov.dto.BossResponse;
import com.tarkovcommunity.tarkov.dto.HideoutStationResponse;
import com.tarkovcommunity.tarkov.dto.ItemResponse;
import com.tarkovcommunity.tarkov.dto.QuestResponse;
import com.tarkovcommunity.tarkov.dto.TarkovMapResponse;
import com.tarkovcommunity.tarkov.dto.TraderResponse;
import com.tarkovcommunity.tarkov.dto.WeaponResponse;
import com.tarkovcommunity.tarkov.service.TarkovCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/traders")
    public ApiResponse<List<TraderResponse>> listTraders() {
        return ApiResponse.success(tarkovCatalogService.listTraders());
    }

    @GetMapping("/quests")
    public ApiResponse<List<QuestResponse>> listQuests() {
        return ApiResponse.success(tarkovCatalogService.listQuests());
    }

    @GetMapping("/items")
    public ApiResponse<List<ItemResponse>> listItems() {
        return ApiResponse.success(tarkovCatalogService.listItems());
    }

    @GetMapping("/weapons")
    public ApiResponse<List<WeaponResponse>> listWeapons() {
        return ApiResponse.success(tarkovCatalogService.listWeapons());
    }

    @GetMapping("/ammo")
    public ApiResponse<List<AmmoResponse>> listAmmo() {
        return ApiResponse.success(tarkovCatalogService.listAmmo());
    }

    @GetMapping("/hideout/stations")
    public ApiResponse<List<HideoutStationResponse>> listHideoutStations() {
        return ApiResponse.success(tarkovCatalogService.listHideoutStations());
    }

    @GetMapping("/bosses")
    public ApiResponse<List<BossResponse>> listBosses() {
        return ApiResponse.success(tarkovCatalogService.listBosses());
    }
}
