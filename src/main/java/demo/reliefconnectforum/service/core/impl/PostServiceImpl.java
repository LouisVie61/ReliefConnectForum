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
import demo.reliefconnectforum.service.core.PostDocService;
import demo.reliefconnectforum.service.core.PostService;
import demo.reliefconnectforum.service.event.AIJobService;
import demo.reliefconnectforum.service.event.PostCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PostDocService postDocService;

    @Autowired
    private AIJobService aiJobService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(readOnly = true)
    // Batch fetch technique to avoid N+1 problem
    public Page<PostResponse> getAll(Pageable pageable) {
        Page<Post> posts = postRepository.findAllWithUser(pageable);

        List<UUID> postIds = posts.getContent().stream()
            .map(Post::getId)
            .toList();

        List<Object[]> results = donationRepository.sumDonationsByPostIds(postIds);
        Map<UUID, BigDecimal> totalsMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));

        return posts.map(post ->
            mapToResponse(post, totalsMap.getOrDefault(post.getId(), BigDecimal.ZERO))
        );
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
        PostResponse response = createPost(request);

        applicationEventPublisher.publishEvent(new PostCreatedEvent(response.getId()));

        return response;
    }

    @Override
    @Transactional
    public PostResponse createNoAI(PostRequest request) {
        return createPost(request);
    }

    @Transactional
    protected PostResponse createPost(PostRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(user);
        post.setLocation(request.getLocation());
        post.setContactName(request.getContactName());
        post.setContactPhone(request.getContactPhone());
        post.setTargetAmount(request.getTargetAmount());
        post.setCreatedAt(LocalDateTime.now());
        post.setPostType(PostType.PENDING);

        Post savedPost = postRepository.save(post);
        postDocService.indexPost(savedPost);

        return mapToResponse(savedPost);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"postById", "allPosts", "postsByPlace", "postsByPlaces"}, allEntries = true)
    public PostResponse update(UUID id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
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
        postDocService.indexPost(updatedPost);
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
        postDocService.deletePost(id.toString());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postsByPlace", key = "#place + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PostResponse> findByPlace(String place, Pageable pageable) {
        Page<Post> posts = postRepository.findByLocationWithUser(place, pageable);

        List<UUID> postIds = posts.getContent().stream().map(Post::getId).toList();
        List<Object[]> results = donationRepository.sumDonationsByPostIds(postIds);
        Map<UUID, BigDecimal> totalsMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));

        return posts.map(post -> mapToResponse(post, totalsMap.getOrDefault(post.getId(), BigDecimal.ZERO)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postsByPlaces",
            key = "T(java.util.Arrays).stream(#places).sorted().collect(T(java.util.stream.Collectors).joining(',')) + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PostResponse> findByPlaces(String[] places, Pageable pageable) {
        Page<Post> posts = postRepository.findByLocationInWithUser(places, pageable);

        List<UUID> postIds = posts.getContent().stream().map(Post::getId).toList();
        List<Object[]> results = donationRepository.sumDonationsByPostIds(postIds);
        Map<UUID, BigDecimal> totalsMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));

        return posts.map(post -> mapToResponse(post, totalsMap.getOrDefault(post.getId(), BigDecimal.ZERO)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchByTitleNoElasticsearch(String query, Pageable pageable) {
        Page<Post> posts = postRepository.findByTitleContainingIgnoreCase(query, pageable);

        List<UUID> postIds = posts.getContent().stream().map(Post::getId).toList();
        List<Object[]> results = donationRepository.sumDonationsByPostIds(postIds);
        Map<UUID, BigDecimal> totalsMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));

        return posts.map(post -> mapToResponse(post, totalsMap.getOrDefault(post.getId(), BigDecimal.ZERO)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchByContentNoElasticsearch(String query, Pageable pageable) {
        Page<Post> posts = postRepository.findByContentContainingIgnoreCase(query, pageable);

        List<UUID> postIds = posts.getContent().stream().map(Post::getId).toList();
        List<Object[]> results = donationRepository.sumDonationsByPostIds(postIds);
        Map<UUID, BigDecimal> totalsMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (BigDecimal) row[1]
                ));

        return posts.map(post -> mapToResponse(post, totalsMap.getOrDefault(post.getId(), BigDecimal.ZERO)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> searchByLocationNoElasticsearch(String location, Pageable pageable) {
        return findByPlace(location, pageable); // Already uses PostgreSQL
    }


    // Overloaded version for batch operations (getAll, findByPlace, findByPlaces)
    private PostResponse mapToResponse(Post post, BigDecimal totalDonations) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setLocation(post.getLocation());
        response.setUserId(post.getAuthor().getId());
        response.setUsername(post.getAuthor().getUsername());
        response.setPostType(post.getPostType());
        response.setCreatedAt(post.getCreatedAt());
        response.setTotalDonations(totalDonations);
        return response;
    }

    // Keep existing method for single-post operations (getById)
    private PostResponse mapToResponse(Post post) {
        BigDecimal totalDonations = adminService.getTotalDonationsByPostId(post.getId());
        return mapToResponse(post, totalDonations != null ? totalDonations : BigDecimal.ZERO);
    }
}