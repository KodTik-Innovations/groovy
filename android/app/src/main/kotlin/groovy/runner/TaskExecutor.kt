package groovy.runner

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.supplyAsync
import java.util.concurrent.CompletionException

object TaskExecutor {
    @JvmOverloads
    @JvmStatic
    fun <R> executeAsync(
        callable: Callable<R>,
        callback: Callback<R>? = null,
    ): CompletableFuture<R?> {
        return supplyAsync {
            try {
                return@supplyAsync callable.call()
            } catch (th: Throwable) {
                return@supplyAsync null
            }
        }
            .whenComplete { result, _ -> callback?.complete(result) }
    }

    @JvmOverloads
    @JvmStatic
    fun <R> executeAsyncProvideError(
        callable: Callable<R>,
        callback: CallbackWithError<R>? = null,
    ): CompletableFuture<R?> {
        return supplyAsync {
            try {
                return@supplyAsync callable.call()
            } catch (th: Throwable) {
                throw CompletionException(th)
            }
        }
            .whenComplete { result, throwable ->
                callback?.complete(result, throwable)
            }
    }

    fun interface Callback<R> {
        fun complete(result: R?)
    }

    fun interface CallbackWithError<R> {
        fun complete(
            result: R?,
            error: Throwable?,
        )
    }
}

fun <R : Any?> executeAsync(
    callable: () -> R?,
    callback: (R?) -> Unit,
): CompletableFuture<R?> = TaskExecutor.executeAsync({ callable() }) { callback(it) }

fun <R : Any?> executeAsyncProvideError(
    callable: () -> R?,
    callback: (R?, Throwable?) -> Unit,
): CompletableFuture<R?> =
    TaskExecutor.executeAsyncProvideError({ callable() }) { result, error ->
        callback(result, error)
    }
