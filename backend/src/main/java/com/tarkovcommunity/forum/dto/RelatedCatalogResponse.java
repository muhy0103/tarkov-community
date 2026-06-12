package com.tarkovcommunity.forum.dto;

public record RelatedCatalogResponse(
        String catalogType,
        Long catalogId,
        String name,
        String subtitle,
        String imageUrl,
        String routeKind,
        String relationNote
) {
}
