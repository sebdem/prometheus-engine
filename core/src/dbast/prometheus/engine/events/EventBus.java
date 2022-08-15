package dbast.prometheus.engine.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;


// Why the fuck did i code this?!
//  - Me, five minutes, after doing so.
// Seriously, find a use for this...
public class EventBus {
    public static Map<String, SubscriberList> eventQueue = new HashMap<>();
    public static Stack<Event> unexecutedEvents = new Stack<>();

    public static void subscribe(String event, Function<Event,?> callback) {
        SubscriberList eventSubscribers = eventQueue.get(event);
        if (eventSubscribers == null) {
            eventSubscribers = new SubscriberList();
            eventQueue.put(event, eventSubscribers);
        }
        eventSubscribers.add(callback);
    }

    // does the work
    public static void update(float delta) {
        while (!unexecutedEvents.empty()) {
            Event executingEvent = unexecutedEvents.pop();
            SubscriberList eventSubscribers = eventQueue.get(executingEvent.getKey());
            // TODO maybe, this must be reversed?
            for(int i = 0; i < eventSubscribers.size() && !executingEvent.cancelled; i++) {
                eventSubscribers.get(i).apply(executingEvent);
            }
        }
    }

    public static void trigger(Event event) {
        unexecutedEvents.push(event);
    }

}
