package org.example.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DemoService {

    private final UserRepository userRepository;
    private final Tracer tracer;
    private final MeterRegistry meterRegistry;

    public List<User> getUsers() {
        Span span = tracer.nextSpan().name("DemoService.getUsers").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            log.info("Fetching users from database");

            List<User> users = userRepository.findAll();

            span.tag("users.count", String.valueOf(users.size()));
            log.info("Fetched {} users", users.size());

            Counter.builder("demo.users.fetched.total")
                    .description("Total number of users fetched")
                    .tag("source", "database")
                    .register(meterRegistry)
                    .increment(users.size());

            return users;
        } catch (Exception e) {
            span.error(e);
            log.error("Error fetching users", e);
            throw e;
        } finally {
            span.end();
        }
    }

    public BusinessDecisionResponse processDecision(int amount, String customerTier) {
        String normalizedTier = normalizeTier(customerTier);
        Timer.Sample sample = Timer.start(meterRegistry);
        Span span = tracer.nextSpan()
                .name("DemoService.processDecision")
                .tag("business.amount", String.valueOf(amount))
                .tag("business.customer_tier", normalizedTier)
                .start();

        String decision = "unknown";
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            log.info("Business decision started: amount={}, customerTier={}", amount, normalizedTier);

            if (amount < 0) {
                decision = "rejected";
                span.tag("business.decision", decision);
                span.event("negative_amount_rejected");
                incrementDecisionMetric(decision, normalizedTier);
                log.warn("Decision branch rejected: negative amount={}", amount);
                return new BusinessDecisionResponse(decision, "Amount must be positive", amount, normalizedTier);
            } else if ("vip".equals(normalizedTier) && amount >= 10_000) {
                decision = "manual_review";
                span.tag("business.decision", decision);
                span.event("vip_high_value_manual_review");
                incrementDecisionMetric(decision, normalizedTier);
                log.info("Decision branch manual_review: vip high value amount={}", amount);
                return new BusinessDecisionResponse(decision, "VIP high-value request requires manual review", amount, normalizedTier);
            } else if (amount >= 5_000) {
                decision = "discount";
                span.tag("business.decision", decision);
                span.event("discount_approved");
                incrementDecisionMetric(decision, normalizedTier);
                log.info("Decision branch discount: amount={} customerTier={}", amount, normalizedTier);
                return new BusinessDecisionResponse(decision, "Discount approved for large request", amount, normalizedTier);
            } else {
                decision = "standard";
                span.tag("business.decision", decision);
                span.event("standard_processing");
                incrementDecisionMetric(decision, normalizedTier);
                log.info("Decision branch standard: amount={} customerTier={}", amount, normalizedTier);
                return new BusinessDecisionResponse(decision, "Standard processing", amount, normalizedTier);
            }
        } catch (Exception e) {
            decision = "error";
            span.tag("business.decision", decision);
            span.error(e);
            incrementDecisionMetric(decision, normalizedTier);
            log.error("Business decision failed: amount={} customerTier={}", amount, normalizedTier, e);
            throw e;
        } finally {
            sample.stop(Timer.builder("business.decision.duration")
                    .description("Business decision processing duration")
                    .tag("decision", decision)
                    .tag("customer_tier", normalizedTier)
                    .register(meterRegistry));
            span.end();
        }
    }

    private void incrementDecisionMetric(String decision, String customerTier) {
        Counter.builder("business.decision.requests.total")
                .description("Business decision requests by branch")
                .tag("decision", decision)
                .tag("customer_tier", customerTier)
                .register(meterRegistry)
                .increment();
    }

    private String normalizeTier(String customerTier) {
        if (customerTier == null || customerTier.isBlank()) {
            return "regular";
        }
        return customerTier.trim().toLowerCase();
    }

    public record BusinessDecisionResponse(
            String decision,
            String message,
            int amount,
            String customerTier
    ) {
    }
}
