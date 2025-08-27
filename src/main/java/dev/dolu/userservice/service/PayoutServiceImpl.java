package dev.dolu.userservice.service;

import dev.dolu.userservice.models.PayoutInfo;
import dev.dolu.userservice.models.PayoutInfoRequest;
import dev.dolu.userservice.models.PayoutInfoResponse;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PayoutServiceImpl implements PayoutService {

    private final UserRepository userRepository;
    private final WebClient webClient;
    private final String paymentBearerToken;
    private final String paymentServiceBaseUrl;
    private final String payoutProfileResolvePath;

    public PayoutServiceImpl(UserRepository userRepository, WebClient.Builder webClientBuilder,
                             @Value("${payment.service.bearer-token}") String paymentBearerToken,
                             @Value("${payment.service.base-url}") String paymentServiceBaseUrl,
                             @Value("${payment.service.payout-profile-resolve-path:/payout-profile/resolve}") String payoutProfileResolvePath) {
        this.userRepository = userRepository;
        this.paymentBearerToken = paymentBearerToken;
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
        this.payoutProfileResolvePath = payoutProfileResolvePath;
        this.webClient = webClientBuilder.baseUrl(paymentServiceBaseUrl).build();
    }

    @Override
    public PayoutInfoResponse resolveAndSavePayoutInfo(UUID userId, PayoutInfoRequest request) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Build payload for payment service
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("userId", userId.toString());
        payload.put("accountNumber", request.getAccountNumber());
        payload.put("bankCode", request.getBankCode());
        if (request.getBvn() != null) payload.put("bvn", request.getBvn());

        Map<String, Object> response;
        try {
            response = webClient.post()
                    .uri(payoutProfileResolvePath)
                    .header("Authorization", "Bearer " + paymentBearerToken)
                    .body(Mono.just(payload), Map.class)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (Exception ex) {
            return new PayoutInfoResponse(false, null);
        }

        if (response == null || response.get("details") == null) {
            return new PayoutInfoResponse(false, null);
        }

        Map<String, Object> details = (Map<String, Object>) response.get("details");
        String accountName = (String) details.get("account_name");

        PayoutInfo payoutInfo = user.getPayoutInfo();
        if (payoutInfo == null) payoutInfo = new PayoutInfo();
        payoutInfo.setAccountNumber(request.getAccountNumber());
        payoutInfo.setBankCode(request.getBankCode());
        payoutInfo.setBvn(request.getBvn());
        payoutInfo.setAccountHolderName(accountName);
        payoutInfo.setBankName((String) details.get("bank_name"));
        payoutInfo.setRecipientCode((String) response.get("recipientCode"));
        payoutInfo.setVerified(true);
        payoutInfo.setLastUpdated(LocalDateTime.now());
        user.setPayoutInfo(payoutInfo);
        user.setPaymentVerified(true);
        userRepository.save(user);
        return new PayoutInfoResponse(true, accountName);
    }

    @Override
    public PayoutInfoResponse getPayoutInfo(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getPayoutInfo() == null || user.getPayoutInfo().getAccountNumber() == null) {
            return new PayoutInfoResponse(false, null);
        }
        return new PayoutInfoResponse(true, user.getPayoutInfo().getAccountHolderName());
    }
}
