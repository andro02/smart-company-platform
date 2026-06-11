package com.smartcompany.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FACTORY_HEARTBEAT_QUEUE  = "factory.heartbeat";
    public static final String FACTORY_PRODUCTION_QUEUE = "factory.production";
    public static final String FACTORY_EXCHANGE         = "factory.exchange";

    public static final String WAREHOUSE_HEARTBEAT_QUEUE   = "warehouse.heartbeat";
    public static final String WAREHOUSE_TEMPERATURE_QUEUE = "warehouse.temperature";
    public static final String WAREHOUSE_EXCHANGE          = "warehouse.exchange";

    @Bean
    public Queue heartbeatQueue() {
        return QueueBuilder.durable(FACTORY_HEARTBEAT_QUEUE).build();
    }

    @Bean
    public Queue productionQueue() {
        return QueueBuilder.durable(FACTORY_PRODUCTION_QUEUE).build();
    }

    @Bean
    public TopicExchange factoryExchange() {
        return new TopicExchange(FACTORY_EXCHANGE);
    }

    @Bean
    public Binding heartbeatBinding(Queue heartbeatQueue, TopicExchange factoryExchange) {
        return BindingBuilder.bind(heartbeatQueue).to(factoryExchange).with("factory.heartbeat");
    }

    @Bean
    public Binding productionBinding(Queue productionQueue, TopicExchange factoryExchange) {
        return BindingBuilder.bind(productionQueue).to(factoryExchange).with("factory.production");
    }

    @Bean
    public Queue warehouseHeartbeatQueue() {
        return QueueBuilder.durable(WAREHOUSE_HEARTBEAT_QUEUE).build();
    }

    @Bean
    public Queue warehouseTemperatureQueue() {
        return QueueBuilder.durable(WAREHOUSE_TEMPERATURE_QUEUE).build();
    }

    @Bean
    public TopicExchange warehouseExchange() {
        return new TopicExchange(WAREHOUSE_EXCHANGE);
    }

    @Bean
    public Binding warehouseHeartbeatBinding(Queue warehouseHeartbeatQueue, TopicExchange warehouseExchange) {
        return BindingBuilder.bind(warehouseHeartbeatQueue).to(warehouseExchange).with("warehouse.heartbeat");
    }

    @Bean
    public Binding warehouseTemperatureBinding(Queue warehouseTemperatureQueue, TopicExchange warehouseExchange) {
        return BindingBuilder.bind(warehouseTemperatureQueue).to(warehouseExchange).with("warehouse.temperature");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}