package demo.reliefconnectforum.service.core.impl;

import demo.reliefconnectforum.dto.request.PostRequest;
import demo.reliefconnectforum.dto.response.PostResponse;
import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.entity.User;
import demo.reliefconnectforum.repository.DonationRepository;
import demo.reliefconnectforum.repository.PostRepository;
import demo.reliefconnectforum.repository.UserRepository;
import demo.reliefconnectforum.service.core.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getAll(Pageable pageable) {
        return postRepository.findAllWithUser(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getById(UUID id) {
        Post post = postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return mapToResponse(post);
    }

    @Override
    public PostResponse create(PostRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(user);
        post.setCreatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost);
    }

    @Override
    public PostResponse update(UUID id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        Post updatedPost = postRepository.save(post);
        return mapToResponse(updatedPost);
    }

    public void delete(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> findByPlace(String place, Pageable pageable) {
        return postRepository.findByLocationWithUser(place, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> findByPlaces(String[] places, Pageable pageable) {
        return postRepository.findByLocationInWithUser(places, pageable)
                .map(this::mapToResponse);
    }

    private PostResponse mapToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setLocation(post.getLocation());
        response.setUserId(post.getAuthor().getId());
        response.setUsername(post.getAuthor().getUsername());
        response.setCreatedAt(post.getCreatedAt());

        BigDecimal totalDonations = donationRepository.sumDonationsByPostId(post.getId());
        response.setTotalDonations(totalDonations != null ? totalDonations : BigDecimal.ZERO);

        return response;
    }
}