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
                        obj[0].toString(),
                        ((Number) obj[1]).doubleValue(),
                        ((Number) obj[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<TopProductResponse> getTopProductsForSeller(Long sellerId) {
        return orderRepository.getProductStatisticsBySeller(sellerId).stream()
                .map(obj -> new TopProductResponse(
                        obj[0].toString(),
                        ((Number) obj[1]).longValue(),
                        ((Number) obj[2]).doubleValue()
                ))
                .collect(Collectors.toList());
    }
}