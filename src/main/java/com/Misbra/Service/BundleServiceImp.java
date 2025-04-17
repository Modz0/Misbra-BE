package com.Misbra.Service;

import com.Misbra.DTO.SessionBundleDTO;
import com.Misbra.Entity.SessionBundle;
import com.Misbra.Mapper.SessionBundleMapper;
import com.Misbra.Repository.SessionBundleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BundleServiceImp implements BundleService {


    private final SessionBundleRepository sessionBundleRepository;
    private final SessionBundleMapper bundleMapper ;


    public BundleServiceImp(SessionBundleRepository sessionBundleRepository, SessionBundleMapper bundleMapper) {
        this.sessionBundleRepository = sessionBundleRepository;
        this.bundleMapper = bundleMapper;
    }
    @Override
    public List<SessionBundleDTO> getAllActiveBundles() {
        return sessionBundleRepository.findAll()
                .stream()
                .filter(SessionBundle::isActive)
                .map(bundleMapper::toDTO)
                .toList();
    }
    @Override
    public Optional<SessionBundleDTO> getBundleById(String bundleId) {
        return sessionBundleRepository.findById(bundleId)
                .filter(SessionBundle::isActive)
                .map(bundleMapper::toDTO);
    }
    @Override
    public SessionBundleDTO createBundle(SessionBundleDTO sessionBundleDTO){
        SessionBundle sessionBundle = bundleMapper.toEntity(sessionBundleDTO);
sessionBundleRepository.save(sessionBundle);
return bundleMapper.toDTO(sessionBundle);
    }


}
