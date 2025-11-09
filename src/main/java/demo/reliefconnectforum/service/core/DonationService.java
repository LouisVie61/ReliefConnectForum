package demo.reliefconnectforum.service.core;

import demo.reliefconnectforum.dto.request.DonationRequest;
import demo.reliefconnectforum.dto.response.DonationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface DonationService {
    Page<DonationResponse> getAll(Pageable pageable);
    DonationResponse getById(UUID id);
    DonationResponse create(DonationRequest request);
    DonationResponse update(UUID id, DonationRequest request);
    void delete(UUID id);
}
