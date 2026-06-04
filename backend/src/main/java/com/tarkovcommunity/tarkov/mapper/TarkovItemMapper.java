package com.tarkovcommunity.tarkov.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tarkovcommunity.tarkov.entity.TarkovItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TarkovItemMapper extends BaseMapper<TarkovItem> {
}
