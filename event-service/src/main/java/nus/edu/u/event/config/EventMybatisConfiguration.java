// package nus.edu.u.event.config;

// import static nus.edu.u.common.constant.Constants.SESSION_TENANT_ID;

// import cn.dev33.satoken.stp.StpUtil;
// import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
// import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
// import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
// import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
// import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
// import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
// import java.time.LocalDateTime;
// import net.sf.jsqlparser.expression.Expression;
// import net.sf.jsqlparser.expression.LongValue;
// import org.apache.ibatis.reflection.MetaObject;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;

// /**
//  * Event service specific MyBatis configuration that relaxes the tenant checks.
//  */
// @Configuration
// public class EventMybatisConfiguration {

//     private static final Long DEFAULT_TENANT_ID = 1L;

//     @Bean
//     @Primary
//     public MybatisPlusInterceptor eventMybatisPlusInterceptor() {
//         MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//         interceptor.addInnerInterceptor(
//                 new TenantLineInnerInterceptor(
//                         new TenantLineHandler() {
//                             @Override
//                             public Expression getTenantId() {
//                                 return new LongValue(resolveTenantId());
//                             }

//                             @Override
//                             public String getTenantIdColumn() {
//                                 return "tenant_id";
//                             }

//                             @Override
//                             public boolean ignoreTable(String tableName) {
//                                 return "sys_dict_data".equals(tableName)
//                                         || "sys_dict_type".equals(tableName)
//                                         || "sys_tenant".equals(tableName)
//                                         || "sys_permission".equals(tableName);
//                             }
//                         }));

//         interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
//         interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
//         return interceptor;
//     }

//     @Bean
//     @Primary
//     public MetaObjectHandler eventMetaObjectHandler() {
//         return new MetaObjectHandler() {
//             @Override
//             public void insertFill(MetaObject metaObject) {
//                 this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
//                 this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
//                 this.strictInsertFill(metaObject, "creator", String.class, resolveUserId());
//                 this.strictInsertFill(metaObject, "updater", String.class, resolveUserId());
//                 if (metaObject.hasSetter("tenantId")) {
//                     metaObject.setValue("tenantId", resolveTenantId());
//                 }
//                 metaObject.setValue("tenant_id", resolveTenantId());
//                 this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
//             }

//             @Override
//             public void updateFill(MetaObject metaObject) {
//                 this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
//                 this.strictUpdateFill(metaObject, "updater", String.class, resolveUserId());
//             }
//         };
//     }

//     private Long resolveTenantId() {
//         try {
//             Object tenantIdObject = StpUtil.getSession().get(SESSION_TENANT_ID);
//             if (tenantIdObject != null) {
//                 return Long.parseLong(tenantIdObject.toString());
//             }
//         } catch (Exception ignored) {
//         }
//         return DEFAULT_TENANT_ID;
//     }

//     private String resolveUserId() {
//         try {
//             Object loginId = StpUtil.getLoginId();
//             if (loginId != null) {
//                 return String.valueOf(loginId);
//             }
//         } catch (Exception ignored) {
//         }
//         return "system";
//     }
// }
