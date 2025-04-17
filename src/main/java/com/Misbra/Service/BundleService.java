package com.Misbra.Service;

import com.Misbra.DTO.SessionBundleDTO;

import java.util.List;
import java.util.Optional;

public interface BundleService {
    List<SessionBundleDTO> getAllActiveBundles();
    Optional<SessionBundleDTO> getBundleById(String bundleId);
    SessionBundleDTO createBundle(SessionBundleDTO sessionBundleDTO);
}
