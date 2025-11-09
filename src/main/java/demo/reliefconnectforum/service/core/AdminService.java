package demo.reliefconnectforum.service.core;

import demo.reliefconnectforum.dto.response.DonationResponse;
import demo.reliefconnectforum.dto.response.DonationStatistic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public interface AdminService {


    // Statistics and advanced queries
    BigDecimal getTotalDonationsByPostId(UUID postId);
    Page<DonationStatistic> getDonationStatisticsByPost(Pageable pageable);
    Page<DonationResponse> findByLocation(String location, Pageable pageable);
    Page<DonationResponse> findByLocations(String[] locations, Pageable pageable);
    Page<DonationResponse> findByMinAmount(BigDecimal minAmount, Pageable pageable);
}
