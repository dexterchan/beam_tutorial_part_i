package io.exp.pubsubinjector;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PubsubMessage;
import io.exp.beampoc.stream.PI.Model.PIInstructionFactory;
import io.exp.beampoc.stream.PI.Model.PiInstruction;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PiPubSubInjector {
    private static Pubsub pubsub;
    private static Random random = new Random();
    private static String topic;
    private static String project;

    // QPS ranges from 800 to 1000.
    private static final int MIN_QPS = 800;
    private static final int QPS_RANGE = 200;
    // How long to sleep, in ms, between creation of the threads that make API requests to PubSub.
    private static final int THREAD_SLEEP_MS = 500;

    /**
     * Generate a pi event.
     */
    private static String generateEvent(Long currTime, int delayInMillis) {
        PiInstruction pi = PIInstructionFactory.createInstruction(PIInstructionFactory.SupportedSeries[1], 1000);

        return pi.toString();
    }

    public static void publishData(int numMessages, int delayInMillis) throws IOException {
        List<PubsubMessage> pubsubMessages = new ArrayList<>();

        for (int i = 0; i < Math.max(1, numMessages); i++) {
            Long currTime = System.currentTimeMillis();
            String message = generateEvent(currTime, delayInMillis);
            PubsubMessage pubsubMessage = new PubsubMessage().encodeData(message.getBytes("UTF-8"));
            pubsubMessage.setAttributes(
                    ImmutableMap.of(
                            InjectorConstant.TIMESTAMP_ATTRIBUTE,
                            Long.toString((currTime - delayInMillis) / 1000 * 1000)));
            if (delayInMillis != 0) {
                System.out.println(pubsubMessage.getAttributes());
                System.out.println("late data for: " + message);
            }
            pubsubMessages.add(pubsubMessage);
        }

        PublishRequest publishRequest = new PublishRequest();
        publishRequest.setMessages(pubsubMessages);
        pubsub.projects().topics().publish(topic, publishRequest).execute();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: PiPubSubInjector project-name (topic-name) numMessages");
            System.exit(1);
        }

        project = args[0];
        String topicName = args[1];
        int numMessages = Integer.parseInt(args[2]);

        pubsub = InjectorUtils.getClient();
        // Create the PubSub topic as necessary.
        topic = InjectorUtils.getFullyQualifiedTopicName(project, topicName);
        InjectorUtils.createTopic(pubsub, topic);
        System.out.println("Injecting to topic: " + topic);

        Thread t=new Thread(
                () -> {
                    try {
                        publishData(numMessages, 0);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                });
        t.start();

        t.join();
    }
}
