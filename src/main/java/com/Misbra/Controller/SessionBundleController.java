
package com.Misbra.Controller;


import com.Misbra.DTO.SessionBundleDTO;
import com.Misbra.Service.BundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bundles")
@RequiredArgsConstructor
public class SessionBundleController {

    private final BundleService bundleService;

    @GetMapping("/active-bundles")
    public ResponseEntity<List<SessionBundleDTO>> createBundle() {
        return ResponseEntity.ok( bundleService.getAllActiveBundles());
    }
    @PostMapping("/add")
    public ResponseEntity<SessionBundleDTO> createBundle(@RequestBody SessionBundleDTO sessionBundleDTO) {
        return ResponseEntity.ok(bundleService.createBundle(sessionBundleDTO));
    }

}
