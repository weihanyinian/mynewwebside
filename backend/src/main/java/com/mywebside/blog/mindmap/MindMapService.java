package com.mywebside.blog.mindmap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywebside.blog.common.BusinessException;
import com.mywebside.blog.mindmap.dto.MindMapCreateRequest;
import com.mywebside.blog.mindmap.dto.MindMapDetailDto;
import com.mywebside.blog.mindmap.dto.MindMapListItemDto;
import com.mywebside.blog.mindmap.dto.MindMapPatchRequest;
import com.mywebside.blog.persistence.entity.MindMapEntity;
import com.mywebside.blog.persistence.mapper.MindMapEntityMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MindMapService {

  private static final String DEFAULT_TITLE = "未命名思维导图";
  private static final String EMPTY_SCENE =
      "{\"type\":\"excalidraw\",\"version\":2,\"source\":\"mywebside\",\"elements\":[],"
          + "\"appState\":{\"theme\":\"light\",\"viewBackgroundColor\":\"#ffffff\"},\"files\":{}}";

  private final MindMapEntityMapper mindMapMapper;

  public MindMapService(MindMapEntityMapper mindMapMapper) {
    this.mindMapMapper = mindMapMapper;
  }

  public List<MindMapListItemDto> listMine(long userId) {
    return mindMapMapper.selectList(
            new LambdaQueryWrapper<MindMapEntity>()
                .eq(MindMapEntity::getUserId, userId)
                .orderByDesc(MindMapEntity::getUpdatedAt)
        ).stream()
        .map(e -> new MindMapListItemDto(e.getId(), e.getTitle(), e.getUpdatedAt()))
        .toList();
  }

  public MindMapDetailDto getMine(long userId, long id) {
    MindMapEntity e = requireMine(userId, id);
    String data = e.getData();
    if (!StringUtils.hasText(data)) {
      data = EMPTY_SCENE;
    }
    return new MindMapDetailDto(e.getId(), e.getTitle(), data, e.getUpdatedAt());
  }

  @Transactional
  public MindMapDetailDto create(long userId, MindMapCreateRequest req) {
    String title = StringUtils.hasText(req.title()) ? req.title().trim() : DEFAULT_TITLE;
    MindMapEntity e = new MindMapEntity();
    e.setUserId(userId);
    e.setTitle(title);
    e.setData(req.data());
    LocalDateTime now = LocalDateTime.now();
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    mindMapMapper.insert(e);
    return new MindMapDetailDto(e.getId(), e.getTitle(), e.getData(), e.getUpdatedAt());
  }

  @Transactional
  public MindMapDetailDto update(long userId, long id, MindMapPatchRequest req) {
    MindMapEntity e = requireMine(userId, id);
    if (!StringUtils.hasText(req.title()) && req.data() == null) {
      throw new BusinessException(400, "请提供标题或画布数据");
    }
    if (StringUtils.hasText(req.title())) {
      e.setTitle(req.title().trim());
    }
    if (req.data() != null) {
      if (!StringUtils.hasText(req.data())) {
        throw new BusinessException(400, "画布数据不能为空");
      }
      e.setData(req.data());
    }
    e.setUpdatedAt(LocalDateTime.now());
    mindMapMapper.updateById(e);
    return new MindMapDetailDto(e.getId(), e.getTitle(), e.getData(), e.getUpdatedAt());
  }

  @Transactional
  public void delete(long userId, long id) {
    MindMapEntity e = requireMine(userId, id);
    mindMapMapper.deleteById(e.getId());
  }

  /** 新建空白导图（前端可直接编辑后 PATCH） */
  @Transactional
  public MindMapDetailDto createBlank(long userId, String title) {
    String t = StringUtils.hasText(title) ? title.trim() : DEFAULT_TITLE;
    MindMapEntity e = new MindMapEntity();
    e.setUserId(userId);
    e.setTitle(t);
    e.setData(EMPTY_SCENE);
    LocalDateTime now = LocalDateTime.now();
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    mindMapMapper.insert(e);
    return new MindMapDetailDto(e.getId(), e.getTitle(), e.getData(), e.getUpdatedAt());
  }

  private MindMapEntity requireMine(long userId, long id) {
    MindMapEntity e = mindMapMapper.selectById(id);
    if (e == null || !e.getUserId().equals(userId)) {
      throw new BusinessException(404, "思维导图不存在");
    }
    return e;
  }
}
