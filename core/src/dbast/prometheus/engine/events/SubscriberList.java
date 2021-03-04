package dbast.prometheus.engine.events;

import java.util.ArrayList;
import java.util.function.Function;

public class SubscriberList extends ArrayList<Function<Event,?>> {
}
