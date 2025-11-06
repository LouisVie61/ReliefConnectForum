package demo.reliefconnectforum.service.core.impl;

import demo.reliefconnectforum.dto.request.DonationRequest;
import demo.reliefconnectforum.dto.response.DonationResponse;
import demo.reliefconnectforum.entity.Donation;
import demo.reliefconnectforum.repository.DonationRepository;
import demo.reliefconnectforum.repository.PostRepository;
import demo.reliefconnectforum.repository.UserRepository;
import demo.reliefconnectforum.service.core.DonationService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DonationServiceImpl implements DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allDonations", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<DonationResponse> getAll(Pageable pageable) {
        return donationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "donationById", key = "#id")
    public DonationResponse getById(UUID id) {
        Optional<Donation> donation = donationRepository.findById(id);
        if (donation.isPresent()) {
            return mapToResponse(donation.get());
        } else {
            throw new RuntimeException("Donation not found with id: " + id);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {
        "allDonations", "donationById", "donationStatisticsByPost",
        "donationsByLocation", "donationsByLocations",
        "totalDonationsByPost", "postById", "allPosts"
    }, allEntries = true)
    public DonationResponse create(DonationRequest request) {
        Donation donation = new Donation();
        donation.setAmount(request.getAmount());
        donation.setUser(userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId())));
        donation.setPost(postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + request.getPostId())));
        donation.setCurrency(request.getCurrency());
        donation.setLocation(request.getLocation());
        donation.setMessage(request.getMessage());

        Donation savedDonation = donationRepository.save(donation);
        return mapToResponse(savedDonation);
    }

    @Override
    @Transactional
    @CacheEvict(value = {
        "allDonations", "donationById", "donationStatisticsByPost",
        "donationsByLocation", "donationsByLocations",
        "totalDonationsByPost", "postById", "allPosts"
    }, allEntries = true)
    public DonationResponse update(UUID id, DonationRequest request) {
        Optional<Donation> existingDonationOpt = donationRepository.findById(id);
        if (existingDonationOpt.isPresent()) {
            Donation existingDonation = existingDonationOpt.get();
            existingDonation.setAmount(request.getAmount());
            existingDonation.setLocation(request.getLocation());
            existingDonation.setMessage(request.getMessage());
            Donation updatedDonation = donationRepository.save(existingDonation);
            return mapToResponse(updatedDonation);
        } else {
            throw new RuntimeException("Donation not found with id: " + id);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {
        "allDonations", "donationById", "donationStatisticsByPost",
        "donationsByLocation", "donationsByLocations",
        "totalDonationsByPost", "postById", "allPosts"
    }, allEntries = true)
    public void delete(UUID id) {
        if (donationRepository.existsById(id)) {
            donationRepository.deleteById(id);
        } else {
            throw new RuntimeException("Donation not found with id: " + id);
        }
    }

    private DonationResponse mapToResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setAmount(donation.getAmount());
        response.setCurrency(donation.getCurrency());
        response.setMessage(donation.getMessage());
        response.setPostId(donation.getPost().getId());
        response.setPostTitle(donation.getPost().getTitle());
        response.setUserId(donation.getUser().getId());
        response.setUsername(donation.getUser().getUsername());
        response.setCreatedAt(donation.getCreatedAt());
        return response;
    }
}
