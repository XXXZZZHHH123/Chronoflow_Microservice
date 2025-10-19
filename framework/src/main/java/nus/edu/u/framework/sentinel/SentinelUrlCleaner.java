package nus.edu.u.framework.sentinel;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Sentinel URL清洗器 用于将带路径参数的URL统一为资源名称 */
@Component
public class SentinelUrlCleaner implements UrlCleaner {

    @Override
    public String clean(String originUrl) {
        if (!StringUtils.hasText(originUrl)) {
            return originUrl;
        }

        // ========== Attendee Service URL Patterns ==========

        // /attendees/list/{eventId}
        if (originUrl.matches("^/attendees/list/\\d+$")) {
            return "/attendees/list/*";
        }

        // /attendees/{attendeeId}
        if (originUrl.matches("^/attendees/\\d+$")) {
            return "/attendees/*";
        }

        // /attendees/bulk/{eventId}
        if (originUrl.matches("^/attendees/bulk/\\d+$")) {
            return "/attendees/bulk/*";
        }

        // ========== Event Service URL Patterns ==========

        // /events/{id}
        if (originUrl.matches("^/events/\\d+$")) {
            return "/events/*";
        }

        // /events/{id}/restore
        if (originUrl.matches("^/events/\\d+/restore$")) {
            return "/events/*/restore";
        }

        // /events/{id}/assignable-groups
        if (originUrl.matches("^/events/\\d+/assignable-groups$")) {
            return "/events/*/assignable-groups";
        }

        // /events/groups/delete/{id}
        if (originUrl.matches("^/events/groups/delete/\\d+$")) {
            return "/events/groups/delete/*";
        }

        // /events/groups/{groupId}/members/{userId}
        if (originUrl.matches("^/events/groups/\\d+/members/\\d+$")) {
            return "/events/groups/*/members/*";
        }

        // /events/groups/{groupId}/members
        if (originUrl.matches("^/events/groups/\\d+/members$")) {
            return "/events/groups/*/members";
        }

        // /events/groups/{groupId}/members/batch
        if (originUrl.matches("^/events/groups/\\d+/members/batch$")) {
            return "/events/groups/*/members/batch";
        }

        // /events/{groupId}/members/{userId}
        if (originUrl.matches("^/events/\\d+/members/\\d+$")) {
            return "/events/*/members/*";
        }

        // ========== User Service URL Patterns ==========

        // /users/permissions/{id}
        if (originUrl.matches("^/users/permissions/\\d+$")) {
            return "/users/permissions/*";
        }

        // /users/roles/{roleId}
        if (originUrl.matches("^/users/roles/\\d+$")) {
            return "/users/roles/*";
        }

        // /users/organizer/update/user/{id}
        if (originUrl.matches("^/users/organizer/update/user/\\d+$")) {
            return "/users/organizer/update/user/*";
        }

        // /users/organizer/delete/user/{id}
        if (originUrl.matches("^/users/organizer/delete/user/\\d+$")) {
            return "/users/organizer/delete/user/*";
        }

        // /users/organizer/restore/user/{id}
        if (originUrl.matches("^/users/organizer/restore/user/\\d+$")) {
            return "/users/organizer/restore/user/*";
        }

        // /users/organizer/disable/user/{id}
        if (originUrl.matches("^/users/organizer/disable/user/\\d+$")) {
            return "/users/organizer/disable/user/*";
        }

        // /users/organizer/enable/user/{id}
        if (originUrl.matches("^/users/organizer/enable/user/\\d+$")) {
            return "/users/organizer/enable/user/*";
        }

        // ========== Task Service URL Patterns  ==========

        // /tasks/{eventId}
        if (originUrl.matches("^/tasks/\\d+$")) {
            return "/tasks/*";
        }

        // /tasks/{eventId}/{taskId}
        if (originUrl.matches("^/tasks/\\d+/\\d+$")) {
            return "/tasks/*/*";
        }

        // /tasks/{eventId}/log/{taskId}
        if (originUrl.matches("^/tasks/\\d+/log/\\d+$")) {
            return "/tasks/*/log/*";
        }

        return originUrl;
    }
}
