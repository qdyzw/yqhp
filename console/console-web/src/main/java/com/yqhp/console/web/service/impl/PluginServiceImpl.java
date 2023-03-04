package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreatePluginParam;
import com.yqhp.console.model.param.UpdatePluginParam;
import com.yqhp.console.model.param.query.PluginPageQuery;
import com.yqhp.console.repository.entity.Plugin;
import com.yqhp.console.repository.entity.ProjectPlugin;
import com.yqhp.console.repository.mapper.PluginMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.PluginService;
import com.yqhp.console.web.service.ProjectPluginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Slf4j
@Service
public class PluginServiceImpl extends ServiceImpl<PluginMapper, Plugin> implements PluginService {

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private ProjectPluginService projectPluginService;

    @Override
    public IPage<Plugin> pageBy(PluginPageQuery query) {
        LambdaQueryWrapper<Plugin> q = new LambdaQueryWrapper<>();
        String keyword = query.getKeyword();
        q.and(StringUtils.hasText(keyword), c -> c
                .like(Plugin::getId, keyword)
                .or()
                .like(Plugin::getName, keyword)
                .or()
                .like(Plugin::getDescription, keyword)
        );
        q.orderByDesc(Plugin::getId);
        return page(new Page<>(query.getPageNumb(), query.getPageSize()), q);
    }

    @Override
    public Plugin createPlugin(CreatePluginParam createPluginParam) {
        Plugin plugin = createPluginParam.convertTo();
        plugin.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        plugin.setCreateBy(currUid);
        plugin.setUpdateBy(currUid);

        try {
            if (!save(plugin)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PLUGIN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLUGIN);
        }

        return getById(plugin.getId());
    }

    @Override
    public Plugin updatePlugin(String id, UpdatePluginParam updatePluginParam) {
        Plugin plugin = getPluginById(id);
        updatePluginParam.update(plugin);
        plugin.setUpdateBy(CurrentUser.id());
        plugin.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(plugin)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PLUGIN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PLUGIN);
        }

        return getById(plugin.getId());
    }

    @Override
    public Plugin getPluginById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PLUGIN_NOT_FOUND));
    }

    @Override
    public void deletePluginById(String id) {
        List<ProjectPlugin> projectPlugins = projectPluginService.listByPluginId(id);
        if (!projectPlugins.isEmpty()) {
            String projectIds = projectPlugins.stream()
                    .map(ProjectPlugin::getProjectId)
                    .collect(Collectors.joining(","));
            String msg = "项目[" + projectIds + "]，正在使用此插件，无法删除";
            throw new ServiceException(ResponseCodeEnum.PROJECT_IN_USE, msg);
        }
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PLUGIN_FAIL);
        }
    }

    @Override
    public List<Plugin> listByProjectId(String projectId) {
        List<String> pluginIds = projectPluginService.listPluginIdByProjectId(projectId);
        return pluginIds.isEmpty() ? new ArrayList<>() : listByIds(pluginIds);
    }
}