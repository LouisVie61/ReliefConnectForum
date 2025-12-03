package demo.reliefconnectforum.service.core.impl;

import demo.reliefconnectforum.dto.response.DonationResponse;
import demo.reliefconnectforum.dto.response.DonationStatistic;
import demo.reliefconnectforum.entity.Donation;
import demo.reliefconnectforum.repository.DonationRepository;
import demo.reliefconnectforum.repository.PostRepository;
import demo.reliefconnectforum.service.core.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "totalDonationsByPost", key = "#postId")
    public BigDecimal getTotalDonationsByPostId(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        return donationRepository.sumDonationsByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "donationStatisticsByPost", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<DonationStatistic> getDonationStatisticsByPost(Pageable pageable) {
        Page<Object[]> statistics = donationRepository.findDonationStatistics(pageable);
        return statistics.map(stat -> {
            DonationStatistic donationStatistic = new DonationStatistic();
            donationStatistic.setPostId((UUID) stat[0]);
            donationStatistic.setTotalAmount((BigDecimal) stat[1]);
            return donationStatistic;
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "donationsByLocation", key = "#location + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<DonationResponse> findByLocation(String location, Pageable pageable) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("location must not be null or empty");
        }

        return donationRepository.findByLocationWithDetails(location, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "donationsByLocations", key = "T(java.util.Arrays).toString(#locations) + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<DonationResponse> findByLocations(String[] locations, Pageable pageable) {
        if (locations == null || locations.length == 0) {
            throw new IllegalArgumentException("locations array must not be null or empty");
        }

        return donationRepository.findByLocationsWithDetails(locations, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    // Cacheable with minAmount as part of the key - consideration
    public Page<DonationResponse> findByMinAmount(BigDecimal minAmount, Pageable pageable) {
        if (minAmount == null || minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum amount must not be null or negative");
        }

        return donationRepository.findByMinAmountWithDetails(minAmount, pageable)
                .map(this::mapToResponse);
    }


    // No-cache versions of the above methods
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDonationsByPostIdNoCache(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }
        return donationRepository.sumDonationsByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponse> findByLocationNoCache(String location, Pageable pageable) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("location must not be null or empty");
        }

        // Use simple query without LEFT JOIN
        Page<Donation> donations = donationRepository.findByLocationSimple(location, pageable);
        return donations.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponse> findByLocationsNoCache(String[] locations, Pageable pageable) {
        if (locations == null || locations.length == 0) {
            throw new IllegalArgumentException("locations array must not be null or empty");
        }

        Page<Donation> donations = donationRepository.findByLocationsSimple(locations, pageable);
        return donations.map(this::mapToResponse);
    }

    private DonationResponse mapToResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setAmount(donation.getAmount());
        response.setMessage(donation.getMessage());
        response.setPostId(donation.getPost().getId());
        response.setPostTitle(donation.getPost().getTitle());
        response.setUserId(donation.getUser().getId());
        response.setUsername(donation.getUser().getUsername());
        response.setCreatedAt(donation.getCreatedAt());
        return response;
    }
}