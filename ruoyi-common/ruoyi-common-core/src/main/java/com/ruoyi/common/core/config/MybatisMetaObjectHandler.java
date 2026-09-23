package com.ruoyi.common.core.config;

import java.util.Date;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

/**
 * MyBatis-Plus 字段自动填充处理器
 * <p>
 * 负责在插入 / 更新时自动填充创建时间、更新时间等公共字段，
 * 与 BaseEntity 上的 @TableField(fill = ...) 注解配合使用。
 *
 * @author bocloud
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler
{
    /**
     * 插入操作时自动填充字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject)
    {
        // 创建时间、更新时间在新增时均填充为当前时间
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    /**
     * 更新操作时自动填充字段
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject)
    {
        // 更新时间在修改时填充为当前时间
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}
