package dev.dolu.userservice.metrics;

import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.repository.VerificationTokenRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * CustomMetricService registers various custom metrics using Micrometer.
 * <p>
 * The following metrics are collected:
 * <ul>
 *   <li><b>users.total</b> (Gauge): Current total number of users.</li>
 *   <li><b>users.registration.count</b> (Counter): Total number of user registrations.</li>
 *   <li><b>user.registration.timer</b> (Timer): Time taken for user registration.</li>
 *   <li><b>users.activation.success.count</b> (Counter): Successful user activations (email verifications).</li>
 *   <li><b>users.activation.failure.count</b> (Counter): Failed user activations.</li>
 *   <li><b>authentication.login.success.count</b> (Counter): Successful login attempts.</li>
 *   <li><b>authentication.login.failure.count</b> (Counter): Failed login attempts.</li>
 *   <li><b>authentication.login.timer</b> (Timer): Time taken for user login.</li>
 *   <li><b>authentication.jwt.validation.failure.count</b> (Counter): JWT validation failures.</li>
 *   <li><b>email.sent.count</b> (Counter): Emails sent successfully.</li>
 *   <li><b>email.failure.count</b> (Counter): Email sending failures.</li>
 *   <li><b>email.sending.timer</b> (Timer): Time taken to send emails.</li>
 *   <li><b>verification.tokens.active</b> (Gauge): Current number of active verification tokens.</li>
 * </ul>
 */
@Service
public class CustomMetricService {

    // Gauge: Total number of users
    private final Gauge usersTotalGauge;

    // User registration metrics
    private final Counter userRegistrationCounter;
    private final Timer userRegistrationTimer;

    // User activation metrics
    private final Counter userActivationSuccessCounter;
    private final Counter userActivationFailureCounter;

    // Authentication metrics
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Timer loginTimer;
    private final Counter jwtValidationFailureCounter;

    // Email service metrics
    private final Counter emailSentCounter;
    private final Counter emailFailureCounter;
    private final Timer emailSendingTimer;

    // Verification token metric: active tokens gauge
    private final Gauge activeVerificationTokensGauge;

    /**
     * Constructs the CustomMetricService and registers all custom metrics.
     *
     * @param meterRegistry     the MeterRegistry to register metrics with
     * @param userRepository    the UserRepository used for the total users gauge
     * @param tokenRepository   the VerificationTokenRepository used for active token gauge
     */
    public CustomMetricService(MeterRegistry meterRegistry,
                               UserRepository userRepository,
                               VerificationTokenRepository tokenRepository) {
        // Register gauge for total users
        this.usersTotalGauge = Gauge.builder("users.total", userRepository, UserRepository::count)
                .description("Total number of users")
                .register(meterRegistry);

        // User Registration Metrics
        this.userRegistrationCounter = Counter.builder("users.registration.count")
                .description("Total number of user registrations")
                .register(meterRegistry);
        this.userRegistrationTimer = Timer.builder("user.registration.timer")
                .description("Time taken for user registration")
                .register(meterRegistry);

        // User Activation Metrics
        this.userActivationSuccessCounter = Counter.builder("users.activation.success.count")
                .description("Total number of successful user activations")
                .register(meterRegistry);
        this.userActivationFailureCounter = Counter.builder("users.activation.failure.count")
                .description("Total number of failed user activations")
                .register(meterRegistry);

        // Authentication Metrics
        this.loginSuccessCounter = Counter.builder("authentication.login.success.count")
                .description("Total number of successful login attempts")
                .register(meterRegistry);
        this.loginFailureCounter = Counter.builder("authentication.login.failure.count")
                .description("Total number of failed login attempts")
                .register(meterRegistry);
        this.jwtValidationFailureCounter = Counter.builder("authentication.jwt.validation.failure.count")
                .description("Total number of JWT validation failures")
                .register(meterRegistry);
        this.loginTimer = Timer.builder("authentication.login.timer")
                .description("Time taken for user login")
                .register(meterRegistry);

        // Email Service Metrics
        this.emailSentCounter = Counter.builder("email.sent.count")
                .description("Total number of emails sent successfully")
                .register(meterRegistry);
        this.emailFailureCounter = Counter.builder("email.failure.count")
                .description("Total number of email send failures")
                .register(meterRegistry);
        this.emailSendingTimer = Timer.builder("email.sending.timer")
                .description("Time taken to send email")
                .register(meterRegistry);

        // Verification Token Metric: Active tokens gauge
        this.activeVerificationTokensGauge = Gauge.builder("verification.tokens.active", tokenRepository, VerificationTokenRepository::count)
                .description("Current number of active verification tokens")
                .register(meterRegistry);
    }

    // ------------------- Increment and Record Methods -------------------

    // User registration methods
    public void incrementUserRegistrationCounter() {
        userRegistrationCounter.increment();
    }

    public void recordUserRegistrationTime(long durationInMillis) {
        userRegistrationTimer.record(durationInMillis, TimeUnit.MILLISECONDS);
    }

    // User activation methods
    public void incrementUserActivationSuccessCounter() {
        userActivationSuccessCounter.increment();
    }

    public void incrementUserActivationFailureCounter() {
        userActivationFailureCounter.increment();
    }

    // Authentication methods
    public void incrementLoginSuccessCounter() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailureCounter() {
        loginFailureCounter.increment();
    }

    public void recordLoginTime(long durationInMillis) {
        loginTimer.record(durationInMillis, TimeUnit.MILLISECONDS);
    }

    public void incrementJwtValidationFailureCounter() {
        jwtValidationFailureCounter.increment();
    }

    // Email service methods
    public void incrementEmailSentCounter() {
        emailSentCounter.increment();
    }

    public void incrementEmailFailureCounter() {
        emailFailureCounter.increment();
    }

    public void recordEmailSendingTime(long durationInMillis) {
        emailSendingTimer.record(durationInMillis, TimeUnit.MILLISECONDS);
    }

    // The activeVerificationTokensGauge is automatically updated by Micrometer.
}