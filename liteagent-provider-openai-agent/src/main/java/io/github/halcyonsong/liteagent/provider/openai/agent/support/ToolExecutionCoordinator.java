package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.core.exception.ToolExecutionException;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 工具并行执行协调器，将多个工具调用提交到线程池并行执行并统一管理超时。
 */
final class ToolExecutionCoordinator {

    private static final ExecutorService TOOL_EXECUTOR =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "liteagent-tool-executor");
                t.setDaemon(true);
                return t;
            });

    private final ToolExecutor executor;
    private final ToolRegistry registry;
    private final long timeoutMillis;

    ToolExecutionCoordinator(ToolExecutor executor, ToolRegistry registry, long timeoutMillis) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * 并行执行全部工具请求，返回与请求顺序一致的 ToolMessage 列表。
     */
    List<ToolMessage> executeAll(List<ToolExecutionRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<ToolMessage>> futures = submitAll(requests);
        awaitCompletion(futures);
        return collectResults(futures);
    }

    private List<CompletableFuture<ToolMessage>> submitAll(List<ToolExecutionRequest> requests) {
        return requests.stream()
                .map(request -> {
                    CompletableFuture<ToolMessage> future = CompletableFuture.supplyAsync(
                            () -> executeSingle(request),
                            TOOL_EXECUTOR
                    );
                    if (timeoutMillis > 0) {
                        future = future.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
                    }
                    return future.exceptionally(ex -> buildErrorMessage(request, ex));
                })
                .collect(Collectors.toList());
    }

    private void awaitCompletion(List<CompletableFuture<ToolMessage>> futures) {
        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        try {
            allDone.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelAll(futures);
            throw new ToolExecutionException("Tool execution interrupted", e);
        } catch (ExecutionException e) {
            // 理论不会走到，exceptionally 已兜底
            throw unwrap(e);
        }
    }

    private ToolMessage buildErrorMessage(ToolExecutionRequest request, Throwable ex) {
        String reason;
        if (ex instanceof TimeoutException) {
            reason = "Tool execution timed out after " + timeoutMillis + "ms";
        } else {
            reason = "Tool execution failed: " + ex.getMessage();
        }
        String content = JsonSupport.toJson(Map.of("error", reason));
        return new ToolMessage(content, request.getId());
    }

    private List<ToolMessage> collectResults(List<CompletableFuture<ToolMessage>> futures) {
        List<ToolMessage> messages = new ArrayList<>(futures.size());
        for (CompletableFuture<ToolMessage> future : futures) {
            messages.add(future.join());
        }
        return List.copyOf(messages);
    }

    private ToolMessage executeSingle(ToolExecutionRequest request) {
        Object result = executor.execute(request, registry);
        String content = OpenAiToolExecutionSupport.stringifyResult(result);
        return new ToolMessage(content, request.getId());
    }

    private static void cancelAll(List<CompletableFuture<ToolMessage>> futures) {
        futures.forEach(f -> f.cancel(true));
    }

    private static ToolExecutionException unwrap(ExecutionException e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        if (cause instanceof ToolExecutionException te) {
            return te;
        }
        return new ToolExecutionException("Failed to execute tools", cause);
    }
}