package cn.refinex.platform.converter;

import cn.refinex.platform.controller.file.dto.response.StorageConfigResponseDTO;
import cn.refinex.platform.entity.SysStorageConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 存储配置对象转换
 *
 * @author Refinex
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface StorageConfigConverter {

    @Mapping(target = "hasAccessKey", expression = "java(src.getAccessKeyCipher() != null)")
    @Mapping(target = "hasSecretKey", expression = "java(src.getSecretKeyCipher() != null)")
    StorageConfigResponseDTO toResponse(SysStorageConfig src);
}

