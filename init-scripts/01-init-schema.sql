-- E-Commerce Order Processing System
-- Init Database Schema

-- Extension UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Order Audit Log Table
CREATE TABLE IF NOT EXISTS order_audit_log (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    total_items INTEGER NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    shipping_cost DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    voucher_discount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(15, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_transaction_id VARCHAR(255),
    is_payment_paid BOOLEAN NOT NULL DEFAULT FALSE,
    shipping_city VARCHAR(255),
    shipping_province VARCHAR(255),
    shipping_country VARCHAR(255),
    fraud_score DOUBLE PRECISION,
    is_suspicious BOOLEAN DEFAULT FALSE,
    source VARCHAR(50) DEFAULT 'WEB',
    order_data JSONB NOT NULL, 
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_order_audit_order_id ON order_audit_log(order_id);
CREATE INDEX idx_order_audit_customer_id ON order_audit_log(customer_id);
CREATE INDEX idx_order_audit_status ON order_audit_log(order_status);
CREATE INDEX idx_order_audit_processed_at ON order_audit_log(processed_at);
CREATE INDEX idx_order_audit_suspicious ON order_audit_log(is_suspicious) WHERE is_suspicious = TRUE;
CREATE INDEX idx_order_audit_fraud_score ON order_audit_log(fraud_score) WHERE fraud_score IS NOT NULL;

CREATE INDEX idx_order_audit_order_data_gin ON order_audit_log USING GIN (order_data);

-- Fraud Alerts Table
CREATE TABLE IF NOT EXISTS fraud_alerts (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    fraud_score DOUBLE PRECISION NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    flags TEXT[], 
    recommendation VARCHAR(20) NOT NULL, 
    reviewed BOOLEAN DEFAULT FALSE,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fraud_alerts_order_id ON fraud_alerts(order_id);
CREATE INDEX idx_fraud_alerts_reviewed ON fraud_alerts(reviewed);
CREATE INDEX idx_fraud_alerts_risk_level ON fraud_alerts(risk_level);

-- Order Processing Stats 
CREATE MATERIALIZED VIEW IF NOT EXISTS order_stats AS
SELECT
    DATE(processed_at) as date,
    COUNT(*) as total_orders,
    SUM(grand_total) as total_revenue,
    AVG(grand_total) as avg_order_value,
    SUM(total_items) as total_items_sold,
    COUNT(CASE WHEN is_suspicious THEN 1 END) as suspicious_orders,
    COUNT(CASE WHEN is_payment_paid THEN 1 END) as paid_orders
FROM order_audit_log
GROUP BY DATE(processed_at)
ORDER BY date DESC;

CREATE UNIQUE INDEX idx_order_stats_date ON order_stats(date);

-- Function to refresh stats 
CREATE OR REPLACE FUNCTION refresh_order_stats()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY order_stats;
END;
$$ LANGUAGE plpgsql;

-- Insert sample statistics 
COMMENT ON TABLE order_audit_log IS 'Audit log of all processed orders from Kafka stream';
COMMENT ON TABLE fraud_alerts IS 'Flagged orders with potential fraud indicators';
COMMENT ON MATERIALIZED VIEW order_stats IS 'Daily aggregated order statistics';
