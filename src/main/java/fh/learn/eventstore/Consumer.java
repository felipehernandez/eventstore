package fh.learn.eventstore;

import com.github.msemys.esjc.CatchUpSubscription;
import com.github.msemys.esjc.CatchUpSubscriptionListener;
import com.github.msemys.esjc.EventStore;
import com.github.msemys.esjc.EventStoreBuilder;
import com.github.msemys.esjc.Position;
import com.github.msemys.esjc.ResolvedEvent;
import com.github.msemys.esjc.SubscriptionDropReason;

import java.util.stream.StreamSupport;

public class Consumer {

    public static void main2(String args[]) {

        EventStore eventstore = EventStoreBuilder.newBuilder()
                                                 .singleNodeAddress("127.0.0.1", 1113)
                                                 .userCredentials("admin", "changeit")
                                                 .build();

        //        eventstore.iterateAllEventsForward(Position.START, 500, false)
        //                  .forEachRemaining(handleEvent());

        Iterable<ResolvedEvent> iterable = () -> eventstore.iterateAllEventsForward(Position.START, 500, false);
        StreamSupport.stream(iterable.spliterator(), false)
                     .filter(resolvedEvent -> "test".equals(resolvedEvent.originalEvent().eventType))
                     .forEach(resolvedEvent -> handleEvent(resolvedEvent));
    }

    public static void main(String args[]) {

        EventStore eventstore = EventStoreBuilder.newBuilder()
                                                 .singleNodeAddress("127.0.0.1", 1113)
                                                 .userCredentials("admin", "changeit")
                                                 .build();

        CatchUpSubscription volatileSubscription =
                eventstore.subscribeToAllFrom(Position.START,
                                              new CatchUpSubscriptionListener() {
                                                  @Override
                                                  public void onEvent(CatchUpSubscription subscription, ResolvedEvent event) {
                                                      if ("test".equals(event.originalEvent().eventType) || "create".equals(event.originalEvent().eventType)) {
                                                          handleEvent(event);
                                                      }
                                                  }

                                                  @Override
                                                  public void onClose(CatchUpSubscription subscription, SubscriptionDropReason reason, Exception exception) {
                                                      System.out.println("Subscription closed: " + reason);
                                                  }
                                              });
    }

    private static void handleEvent(ResolvedEvent resolvedEvent) {
        System.out.format("@%s  id: '%s'; type: '%s'; data: '%s'\n",
                          resolvedEvent.originalPosition,
                          resolvedEvent.originalEvent().eventId,
                          resolvedEvent.originalEvent().eventType,
                          new String(resolvedEvent.originalEvent().data));
    }
}
