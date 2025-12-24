package com.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_type")
    private String eventType; 

    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("source_service")
    private String sourceService; 

    @JsonProperty("order")
    private Order order;

    @JsonProperty("metadata")
    private EventMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventMetadata {
        @JsonProperty("correlation_id")
        private String correlationId;

        @JsonProperty("user_agent")
        private String userAgent;

        @JsonProperty("ip_address")
        private String ipAddress;

        @JsonProperty("version")
        @Builder.Default
        private String version = "1.0";
    }
}
