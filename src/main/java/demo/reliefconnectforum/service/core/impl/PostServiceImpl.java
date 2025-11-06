package demo.reliefconnectforum.service.core.impl;

import demo.reliefconnectforum.Enum.PostType;
import demo.reliefconnectforum.dto.request.PostRequest;
import demo.reliefconnectforum.dto.response.PostResponse;
import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.entity.User;
import demo.reliefconnectforum.repository.DonationRepository;
import demo.reliefconnectforum.repository.PostRepository;
import demo.reliefconnectforum.repository.UserRepository;
import demo.reliefconnectforum.service.core.AdminService;
import demo.reliefconnectforum.service.core.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private AdminService adminService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PostResponse> getAll(Pageable pageable) {
        return postRepository.findAllWithUser(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postById", key = "#id")
    public PostResponse getById(UUID id) {
        Post post = postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return mapToResponse(post);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allPosts", "postsByPlace", "postsByPlaces"}, allEntries = true)
    public PostResponse create(PostRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(user);
        post.setPostType(request.getPostType() != null ? request.getPostType() : PostType.RESCUE);
        post.setLocation(request.getLocation());
        post.setContactName(request.getContactName());
        post.setContactPhone(request.getContactPhone());
        post.setTargetAmount(request.getTargetAmount());
        post.setCreatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"postById", "allPosts"}, allEntries = true)
    public PostResponse update(UUID id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        if (request.getPostType() != null) {
            post.setPostType(request.getPostType());
        }

        if (request.getLocation() != null) {
            post.setLocation(request.getLocation());
        }

        if (request.getContactName() != null) {
            post.setContactName(request.getContactName());
        }

        if (request.getContactPhone() != null) {
            post.setContactPhone(request.getContactPhone());
        }

        if (request.getTargetAmount() != null) {
            post.setTargetAmount(request.getTargetAmount());
        }

        Post updatedPost = postRepository.save(post);
        return mapToResponse(updatedPost);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allPosts", "postById", "postsByPlace", "postsByPlaces"}, allEntries = true)
    public void delete(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postsByPlace", key = "#place + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PostResponse> findByPlace(String place, Pageable pageable) {
        return postRepository.findByLocationWithUser(place, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postsByPlaces",
        key = "T(java.util.Arrays).stream(#places).sorted().collect(T(java.util.stream.Collectors).joining(',')) + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
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

        BigDecimal totalDonations = adminService.getTotalDonationsByPostId(post.getId()); // cached hit here
        response.setTotalDonations(totalDonations != null ? totalDonations : BigDecimal.ZERO);

        return response;
    }
}