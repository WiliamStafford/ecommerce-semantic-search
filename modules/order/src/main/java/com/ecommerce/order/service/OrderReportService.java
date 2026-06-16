package com.ecommerce.order.service;

import com.ecommerce.order.dto.response.StatResponse;
import com.ecommerce.order.dto.response.TopProductResponse;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
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
}