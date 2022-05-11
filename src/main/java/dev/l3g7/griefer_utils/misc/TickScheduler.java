package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class TickScheduler {

    private static final Map<String, Pair<Integer, Map<Runnable, Integer>>> queues = new HashMap<>();
    private static final Map<Runnable, Integer> delayedClientTickRunnables = new HashMap<>();
    private static final Map<Runnable, Integer> delayedRenderTickRunnables = new HashMap<>();

    public static void queue(String queue, Runnable runnable, int delay) {
        queues.computeIfAbsent(queue, q -> Pair.of(0, new HashMap<>())).b.put(runnable, delay);
    }

    public static void runNextTick(Runnable runnable) {
        runLater(runnable, 1);
    }

    public static void runNextRenderTick(Runnable runnable) {
        synchronized (delayedRenderTickRunnables) {
            delayedRenderTickRunnables.put(runnable, 1);
        }
    }

    public static void runLater(Runnable runnable, int delay) {
        delayedClientTickRunnables.put(runnable, delay);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        List<Runnable> runnableList = new ArrayList<>(delayedClientTickRunnables.keySet());
        for (Runnable runnable : runnableList) {
            if (delayedClientTickRunnables.compute(runnable, (r, i) -> i - 1) < 1) {
                delayedClientTickRunnables.remove(runnable);
                runnable.run();
            }
        }

        for (Pair<Integer, Map<Runnable, Integer>> pair : queues.values()) {
            pair.a--;
            Map<Runnable, Integer> map = pair.b;
            if (pair.a < 1 && !map.isEmpty()) {
                Runnable runnable = map.keySet().iterator().next();
                pair.a = map.remove(runnable);
                runnable.run();
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        List<Runnable> runnables = new ArrayList<>();
        synchronized (delayedRenderTickRunnables) {
            List<Runnable> runnableList = new ArrayList<>(delayedRenderTickRunnables.keySet());
            for (Runnable runnable : runnableList) {
                if (delayedRenderTickRunnables.compute(runnable, (r, i) -> i - 1) < 1) {
                    delayedRenderTickRunnables.remove(runnable);
                    runnables.add(runnable);
                }
            }
        }

        runnables.forEach(Runnable::run);
    }

}
