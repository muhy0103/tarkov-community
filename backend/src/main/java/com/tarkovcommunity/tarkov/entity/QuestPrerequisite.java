package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quest_prerequisite")
public class QuestPrerequisite {
    private Long id;
    private Long questId;
    private Long prerequisiteQuestId;
}
