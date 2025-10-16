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
    public BigDecimal getTotalDonationsByPostId(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        return donationRepository.sumDonationsByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
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
    public Page<DonationResponse> findByLocation(String location, Pageable pageable) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("location must not be null or empty");
        }

        return donationRepository.findByLocationWithDetails(location, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponse> findByLocations(String[] locations, Pageable pageable) {
        if (locations == null || locations.length == 0) {
            throw new IllegalArgumentException("locations array must not be null or empty");
        }

        return donationRepository.findByLocationsWithDetails(locations, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponse> findByMinAmount(BigDecimal minAmount, Pageable pageable) {
        if (minAmount == null || minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum amount must not be null or negative");
        }

        return donationRepository.findByMinAmountWithDetails(minAmount, pageable)
                .map(this::mapToResponse);
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