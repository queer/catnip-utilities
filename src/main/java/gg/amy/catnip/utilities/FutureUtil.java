package gg.amy.catnip.utilities;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author amy
 * @since 12/10/18.
 */
public final class FutureUtil {
    private FutureUtil() {
    }
    
    private static <T> CompletableFuture<T> stageToFuture(@Nonnull final CompletionStage<T> stage) {
        if(stage instanceof CompletableFuture) {
            return (CompletableFuture<T>) stage;
        } else {
            return stage.toCompletableFuture();
        }
    }
    
    public static <T> CompletionStage<Void> awaitAll(@Nonnull final CompletionStage<T>[] f) {
        return CompletableFuture.allOf(
                Arrays.stream(f)
                        .map(FutureUtil::stageToFuture)
                        .toArray(CompletableFuture[]::new)
        );
    }
    
    public static <T> CompletionStage<Void> awaitAll(@Nonnull final Collection<CompletionStage<T>> f) {
        return CompletableFuture.allOf(
                f.stream()
                        .map(FutureUtil::stageToFuture)
                        .toArray(CompletableFuture[]::new)
        );
    }
}
