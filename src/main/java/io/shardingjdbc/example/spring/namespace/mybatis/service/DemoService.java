package io.shardingjdbc.example.spring.namespace.mybatis.service;

import io.shardingjdbc.example.spring.namespace.mybatis.entity.Order;
import io.shardingjdbc.example.spring.namespace.mybatis.repository.OrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DemoService {
    
    @Resource
    private OrderRepository orderRepository;
    public void drop(){
        orderRepository.dropTable();
    }
    public void demo() {
        orderRepository.createIfNotExistsTable();
        orderRepository.truncateTable();
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setStatus("2");
            orderRepository.insert(order);
        }
    }
}
