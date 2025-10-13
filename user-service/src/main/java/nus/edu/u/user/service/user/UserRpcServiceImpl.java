package nus.edu.u.user.service.user;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.shared.rpc.user.TenantDTO;
import nus.edu.u.shared.rpc.user.UserInfoDTO;
import nus.edu.u.shared.rpc.user.UserRpcService;
import nus.edu.u.user.domain.dataobject.tenant.TenantDO;
import nus.edu.u.user.domain.dataobject.user.UserDO;
import nus.edu.u.user.mapper.tenant.TenantMapper;
import nus.edu.u.user.mapper.user.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@DubboService
@Slf4j
@RequiredArgsConstructor
public class UserRpcServiceImpl implements UserRpcService {
    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;

    @Override
    public boolean exists(Long userId) {
        if (userId == null) {
            log.warn("exists called with null userId");
            return false;
        }

        try {
            return userMapper.selectById(userId) != null;
        } catch (Exception e) {
            log.error("Error checking user existence for userId: {}", userId, e);
            return false;
        }
    }

    @Override
    public Map<Long, UserInfoDTO> getUsers(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            log.debug("getUsers called with empty userIds");
            return Collections.emptyMap();
        }

        try {
            return userMapper.selectBatchIds(userIds).stream()
                    .map(this::convertToUserInfoDTO)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toMap(UserInfoDTO::getId, user -> user));
        } catch (Exception e) {
            log.error("Error getting users for userIds: {}", userIds, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public TenantDTO getTenantById(Long tenantId) {
        if (tenantId == null) {
            log.warn("getTenantById called with null tenantId");
            return null;
        }

        try {
            TenantDO tenantDO = tenantMapper.selectById(tenantId);
            return convertToTenantDTO(tenantDO);
        } catch (Exception e) {
            log.error("Error getting tenant for tenantId: {}", tenantId, e);
            return null;
        }
    }

    private TenantDTO convertToTenantDTO(TenantDO tenantDO) {
        if (tenantDO == null) {
            return null;
        }

        return TenantDTO.builder()
                .id(tenantDO.getId())
                .name(tenantDO.getName())
                .contactUserId(tenantDO.getContactUserId())
                .contactName(tenantDO.getContactName())
                .contactMobile(tenantDO.getContactMobile())
                .address(tenantDO.getAddress())
                .status(tenantDO.getStatus())
                .tenantCode(tenantDO.getTenantCode())
                .build();
    }

    private UserInfoDTO convertToUserInfoDTO(UserDO userDO) {
        if (userDO == null) {
            return null;
        }

        return UserInfoDTO.builder()
                .id(userDO.getId())
                .username(userDO.getUsername())
                .status(userDO.getStatus())
                .build();
    }
}
