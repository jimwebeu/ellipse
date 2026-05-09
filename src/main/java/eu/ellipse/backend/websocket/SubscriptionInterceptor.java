package eu.ellipse.backend.websocket;

import eu.ellipse.backend.repository.CircleMemberRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SubscriptionInterceptor implements ChannelInterceptor {

    private final CircleMemberRepository circleMemberRepository;

    public SubscriptionInterceptor(CircleMemberRepository circleMemberRepository) {
        this.circleMemberRepository = circleMemberRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            String userId = (String) accessor.getSessionAttributes().get("userId");

            if (destination != null && destination.startsWith("/topic/circle/")) {
                String circleId = destination.replace("/topic/circle/", "");

                try {
                    boolean isMember = circleMemberRepository.existsByCircleIdAndUserId(
                            UUID.fromString(circleId),
                            UUID.fromString(userId)
                    );

                    if (!isMember) {
                        return null;
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }

        return message;
    }
}