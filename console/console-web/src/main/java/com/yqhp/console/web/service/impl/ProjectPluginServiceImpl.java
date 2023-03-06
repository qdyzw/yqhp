package com.yqhp.console.web.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yqhp.auth.model.CurrentUser;
import com.yqhp.common.web.exception.ServiceException;
import com.yqhp.console.model.param.CreateProjectPluginParam;
import com.yqhp.console.model.param.UpdateProjectPluginParam;
import com.yqhp.console.repository.entity.ProjectPlugin;
import com.yqhp.console.repository.mapper.ProjectPluginMapper;
import com.yqhp.console.web.enums.ResponseCodeEnum;
import com.yqhp.console.web.service.ProjectPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jiangyitao
 */
@Service
public class ProjectPluginServiceImpl
        extends ServiceImpl<ProjectPluginMapper, ProjectPlugin>
        implements ProjectPluginService {

    @Autowired
    private Snowflake snowflake;

    @Override
    public void createProjectPlugin(CreateProjectPluginParam param) {
        ProjectPlugin projectPlugin = param.convertTo();
        projectPlugin.setId(snowflake.nextIdStr());

        String currUid = CurrentUser.id();
        projectPlugin.setCreateBy(currUid);
        projectPlugin.setUpdateBy(currUid);

        try {
            if (!save(projectPlugin)) {
                throw new ServiceException(ResponseCodeEnum.SAVE_PROJECT_PLUGIN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PROJECT_PLUGIN);
        }
    }

    @Override
    public void updateProjectPlugin(String id, UpdateProjectPluginParam param) {
        ProjectPlugin projectPlugin = getProjectPluginById(id);
        param.update(projectPlugin);
        projectPlugin.setUpdateBy(CurrentUser.id());
        projectPlugin.setUpdateTime(LocalDateTime.now());

        try {
            if (!updateById(projectPlugin)) {
                throw new ServiceException(ResponseCodeEnum.UPDATE_PROJECT_PLUGIN_FAIL);
            }
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ResponseCodeEnum.DUPLICATE_PROJECT_PLUGIN);
        }
    }

    @Override
    public void deleteProjectPluginById(String id) {
        if (!removeById(id)) {
            throw new ServiceException(ResponseCodeEnum.DEL_PROJECT_PLUGIN_FAIL);
        }
    }

    @Override
    public ProjectPlugin getProjectPluginById(String id) {
        return Optional.ofNullable(getById(id))
                .orElseThrow(() -> new ServiceException(ResponseCodeEnum.PROJECT_PLUGIN_NOT_FOUND));
    }

    @Override
    public List<ProjectPlugin> listByProjectId(String projectId) {
        Assert.hasText(projectId, "projectId must has text");
        LambdaQueryWrapper<ProjectPlugin> query = new LambdaQueryWrapper<>();
        query.eq(ProjectPlugin::getProjectId, projectId);
        return list(query);
    }

    @Override
    public List<String> listPluginIdByProjectId(String projectId) {
        return listByProjectId(projectId).stream()
                .map(ProjectPlugin::getPluginId).collect(Collectors.toList());
    }

    @Override
    public List<ProjectPlugin> listByPluginId(String pluginId) {
        Assert.hasText(pluginId, "pluginId must has text");
        LambdaQueryWrapper<ProjectPlugin> query = new LambdaQueryWrapper<>();
        query.eq(ProjectPlugin::getPluginId, pluginId);
        return list(query);
    }
}
