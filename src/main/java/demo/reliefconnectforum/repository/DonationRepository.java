package demo.reliefconnectforum.repository;

import demo.reliefconnectforum.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    @Query(value = "SELECT SUM(d.amount) FROM donations d WHERE d.post_id = :postId", nativeQuery = true)
    BigDecimal sumDonationsByPostId(@Param("postId") UUID postId);

    @Query(value = "SELECT d.post_id as postId, SUM(d.amount) as totalAmount " +
                   "FROM donations d " +
                   "GROUP BY d.post_id",
           countQuery = "SELECT COUNT(DISTINCT d.post_id) FROM donations d",
           nativeQuery = true)
    Page<Object[]> findDonationStatistics(Pageable pageable);

    @Query(value = "SELECT d.* FROM donations d " +
                   "LEFT JOIN posts p ON d.post_id = p.id " +
                   "LEFT JOIN users u ON d.user_id = u.id " +
                   "WHERE p.location = :location",
           countQuery = "SELECT COUNT(*) FROM donations d LEFT JOIN posts p ON d.post_id = p.id WHERE p.location = :location",
           nativeQuery = true)
    Page<Donation> findByLocationWithDetails(@Param("location") String location, Pageable pageable);

    @Query(value = "SELECT d.* FROM donations d " +
                   "LEFT JOIN posts p ON d.post_id = p.id " +
                   "LEFT JOIN users u ON d.user_id = u.id " +
                   "WHERE p.location IN (:locations)",
           countQuery = "SELECT COUNT(*) FROM donations d LEFT JOIN posts p ON d.post_id = p.id WHERE p.location IN (:location)",
           nativeQuery = true)
    Page<Donation> findByLocationsWithDetails(@Param("locations") String[] locations, Pageable pageable);

    @Query(value = "SELECT d.* FROM donations d " +
                   "LEFT JOIN posts p ON d.post_id = p.id " +
                   "LEFT JOIN users u ON d.user_id = u.id " +
                   "WHERE d.amount >= :amount",
           countQuery = "SELECT COUNT(*) FROM donations d WHERE d.amount >= :amount",
           nativeQuery = true)
    Page<Donation> findByMinAmountWithDetails(@Param("amount") BigDecimal amount, Pageable pageable);

    @Query(value = "SELECT d.post_id, COALESCE(SUM(d.amount), 0) " +
            "FROM donations d " +
            "WHERE d.post_id IN :postIds " +
            "GROUP BY d.post_id",
            nativeQuery = true)
    List<Object[]> sumDonationsByPostIds(@Param("postIds") List<UUID> postIds);

    // No Join Table Technique
    @Query(value = "SELECT d.* FROM donations d WHERE d.location = :location", nativeQuery = true)
    Page<Donation> findByLocationSimple(@Param("location") String location, Pageable pageable);

    @Query(value = "SELECT d.* FROM donations d WHERE d.location IN (:locations)", nativeQuery = true)
    Page<Donation> findByLocationsSimple(@Param("locations") String[] locations, Pageable pageable);

    @Query(value = "SELECT d.* FROM donations d WHERE d.amount >= :amount", nativeQuery = true)
    Page<Donation> findByMinAmountSimple(@Param("amount") BigDecimal amount, Pageable pageable);
}