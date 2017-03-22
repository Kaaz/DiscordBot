package emily.handler.discord;

import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @param <T> type of response
 */
public class RestTask<T> implements Runnable {
    private final static long TIMEOUT = 15L;
    private final RestAction<T> action;
    private final Consumer<T> complete;
    private final ExecutorService pool;

    public RestTask(RestAction<T> action) {

        this.action = action;
        complete = null;
        pool = null;
    }

    public RestTask(ExecutorService pool, RestAction<T> action, Consumer<T> complete) {
        this.pool = pool;
        this.action = action;
        this.complete = complete;
    }

    @Override
    public void run() {
        if (action == null) {
            return;
        }
        Future<T> future = action.submit(true);
        T ret;
        try {
            ret = future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            ret = null;
        }
        if (complete != null && pool != null) {
            final T cb = ret;
            pool.submit(() -> complete.accept(cb));
        }
    }
}
