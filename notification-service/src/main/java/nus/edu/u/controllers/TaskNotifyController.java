package nus.edu.u.controllers;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.common.NewTaskAssignmentDTO;
import nus.edu.u.services.domains.task.TaskAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
@Validated
public class TaskNotifyController {

    private final TaskAssignmentService taskAssignmentService;

    /**
     * Fire WS + PUSH (+ EMAIL if provided in DTO) for a task assignment. Returns per-channel status
     * map e.g. { ws: "ACCEPTED", push: "ACCEPTED", email: "SKIPPED_NO_EMAIL" }
     */
    @PostMapping("/notify-all")
    public ResponseEntity<Map<String, String>> notifyAll(@RequestBody NewTaskAssignmentDTO dto) {
        var result = taskAssignmentService.notifyNewTaskAllChannels(dto);
        return ResponseEntity.accepted().body(result);
    }
}
