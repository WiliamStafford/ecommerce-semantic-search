package com.ecommerce.order.service;

import com.ecommerce.order.dto.response.StatResponse;
import com.ecommerce.order.dto.response.TopProductResponse;
import com.ecommerce.order.dto.response.TopProductResponseAdmin;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReportService {
    private final OrderRepository orderRepository;

    public List<StatResponse> getRevenueByYearForSeller(Long sellerId) {
        return orderRepository.getRevenueStatisticsByYearForSeller(sellerId).stream()
                .map(obj -> new StatResponse(
                        obj[0] != null ? obj[0].toString() : "N/A",
                        obj[1] != null ? ((Number) obj[1]).doubleValue() : 0.0,
                        obj[2] != null ? ((Number) obj[2]).longValue() : 0L
                ))
                .collect(Collectors.toList());
    }

    public List<TopProductResponse> getTopProductsForSeller(Long sellerId) {
        return orderRepository.getProductStatisticsBySeller(sellerId).stream()
                .map(obj -> {
                    String productName = obj[0] != null ? obj[0].toString() : "Unknown";

                    long soldQty = obj[1] != null ? ((Number) obj[1]).longValue() : 0L;
                    double revenue = obj[2] != null ? ((Number) obj[2]).doubleValue() : 0.0;

                    return new TopProductResponse(productName, soldQty, revenue);
                })
                .collect(Collectors.toList());
    }

    public List<StatResponse> getStatsByPeriod(Long sellerId, String periodType) {
        return orderRepository.getStatsByPeriod(sellerId, periodType).stream()
                .map(obj -> new StatResponse(
                        obj[0] != null ? obj[0].toString() : "N/A",
                        obj[1] != null ? ((Number) obj[1]).doubleValue() : 0.0,
                        obj[2] != null ? ((Number) obj[2]).longValue() : 0L
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> calculateRevenueBySeller() {
        List<Object[]> results = orderRepository.calculateRevenueBySeller();
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] row : results) {
            log.info("Processing row: {}", row);
            Map<String, Object> map = new HashMap<>();
            map.put("shopName", row[0] != null ? row[0].toString() : "Chưa đặt tên");
            map.put("sellerId", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            map.put("totalRevenue", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
            map.put("totalOrders", row[3] != null ? ((Number) row[3]).longValue() : 0L);
            log.info("Mapped Result: {}", map);
            mappedResults.add(map);
        }
        return mappedResults;
    }
    public List<TopProductResponseAdmin> getTopProductsForAdmin(Long sellerId) {
        return orderRepository.getProductStatisticsBySellerForAdmin(sellerId).stream()
                .map(obj -> new TopProductResponseAdmin(
                        obj[0] != null ? ((Number) obj[0]).longValue() : 0L,    // productId
                        obj[1] != null ? obj[1].toString() : "Unknown",        // productName
                        obj[2] != null ? ((Number) obj[2]).intValue() : 0,     // totalSold
                        obj[3] != null ? ((Number) obj[3]).doubleValue() : 0.0 // totalRevenue
                ))
                .collect(Collectors.toList());
    }
}