package com.example.farmerbackend.service;

import com.example.farmerbackend.entity.*;
import com.example.farmerbackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(CustomerOrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartItemRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CustomerOrder createOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartRepository.findByUserUserId(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setStatus("PLACED");

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();

            if (product.getQuantityAvailable() < requestedQuantity) {
                throw new RuntimeException("Insufficient quantity for " + product.getProductName());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(requestedQuantity);
            item.setPrice(product.getPrice());

            orderItems.add(item);

            total += product.getPrice() * requestedQuantity;

            product.setQuantityAvailable(product.getQuantityAvailable() - requestedQuantity);
            productRepository.save(product);
        }

        order.setTotalAmount(total);
        order.setItems(orderItems);

        CustomerOrder savedOrder = orderRepository.save(order);
        cartRepository.deleteByUserUserId(userId);

        return savedOrder;
    }

    @Transactional
    public CustomerOrder createOrder(Long userId, String deliveryAddress, String paymentMethod) {
        CustomerOrder order = createOrder(userId);
        if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
            order.setDeliveryAddress(deliveryAddress);
        }
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            order.setPaymentMethod(paymentMethod);
        }
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getUserOrders(Long userId) {
        return orderRepository.findByUserUserIdOrderByOrderIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public CustomerOrder updateStatus(Long orderId, String status) {
        CustomerOrder order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}