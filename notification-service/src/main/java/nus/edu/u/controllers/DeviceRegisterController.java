package nus.edu.u.controllers;

import lombok.RequiredArgsConstructor;
import nus.edu.u.domain.dto.common.DeviceRegisterDTO;
import nus.edu.u.enums.push.PushPlatform;
import nus.edu.u.services.push.DeviceRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications/push/devices")
public class DeviceRegisterController {

    private final DeviceRegistryService deviceRegistryService;

    @PostMapping("/register")
    public ResponseEntity<?> registerDevice(
            @RequestParam("userId") String userId, // or resolve from security
            @RequestBody DeviceRegisterDTO dto) {
        if (dto.getPlatform() == null) dto.setPlatform(PushPlatform.WEB);
        deviceRegistryService.register(userId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revokeByToken(@RequestParam("token") String token) {
        deviceRegistryService.revokeByToken(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke-all")
    public ResponseEntity<Void> revokeAll(@RequestParam("userId") String userId) {
        deviceRegistryService.revokeAllForUser(userId);
        return ResponseEntity.ok().build();
    }
}
