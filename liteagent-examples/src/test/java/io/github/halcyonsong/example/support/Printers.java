package io.github.halcyonsong.example.support;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;

public final class Printers {

    private static final String SEP = "════════════════════════════════════════";

    private Printers() {
    }

    // ─── 同步响应 ──────────────────────────────────────────────

    public static void printChatResponse(OpenAiChatCompletionResponse response) {
        System.out.println(SEP);
        System.out.println("Response id  = " + response.getBaseResponse().getId());
        System.out.println("Model        = " + response.getBaseResponse().getModel());

        for (ChatChoice choice : response.getChoices()) {
            System.out.println("  choice[" + choice.getIndex() + "] finish_reason = " + choice.getFinishReason());

            for (Message message : choice.getChatResponse().getMessages()) {
                printMessage(message, 2);
            }
        }

        if (response.getUsage() != null) {
            System.out.println("Usage: prompt=" + response.getUsage().getPromptTokens()
                    + " completion=" + response.getUsage().getCompletionTokens()
                    + " total=" + response.getUsage().getTotalTokens());
        }
        System.out.println(SEP);
    }

    public static void printMessage(Message message, int indent) {
        String prefix = " ".repeat(indent);
        System.out.println(prefix + "role = " + message.getRole());

        if (message.getContent() != null && !message.getContent().isBlank()) {
            System.out.println(prefix + "content = " + message.getContent());
        }

        if (message instanceof AssistantResponseMessage arm) {
            if (arm.getReasoningContent() != null && !arm.getReasoningContent().isBlank()) {
                System.out.println(prefix + "reasoning = " + arm.getReasoningContent());
            }

            if (arm.getToolCalls() != null && !arm.getToolCalls().isEmpty()) {
                for (ToolCall toolCall : arm.getToolCalls()) {
                    System.out.println(prefix + "tool_call:");
                    System.out.println(prefix + "  id   = " + toolCall.getId());
                    System.out.println(prefix + "  type = " + toolCall.getType());
                    if (toolCall.getFunction() != null) {
                        System.out.println(prefix + "  name = " + toolCall.getFunction().getName());
                        System.out.println(prefix + "  args = " + toolCall.getFunction().getArguments());
                    }
                }
            }
        }
    }

    // ─── 流式响应 ──────────────────────────────────────────────

    /**
     * 打印流式 chunk 的完整信息（id / delta / finishReason / usage）。
     */
    public static void printStreamChunk(OpenAiStreamCompletionResponse response) {
        System.out.println(SEP);
        System.out.println("Stream id   = " + response.getBaseResponse().getId());
        System.out.println("Model       = " + response.getBaseResponse().getModel());

        for (StreamChoice choice : response.getChoices()) {
            System.out.println("  choice[" + choice.getIndex() + "] finish_reason = " + choice.getFinishReason());

            StreamDelta delta = choice.getDelta();
            if (delta != null) {
                if (delta.getRole() != null) {
                    System.out.println("    role = " + delta.getRole());
                }
                if (delta.getContent() != null && !delta.getContent().isBlank()) {
                    System.out.println("    content = " + delta.getContent());
                }
                if (delta.getReasoningContent() != null && !delta.getReasoningContent().isBlank()) {
                    System.out.println("    reasoning = " + delta.getReasoningContent());
                }
                if (delta.getToolCalls() != null && !delta.getToolCalls().isEmpty()) {
                    for (ToolCall toolCall : delta.getToolCalls()) {
                        System.out.println("    tool_call delta:");
                        System.out.println("      index = " + toolCall.getIndex());
                        System.out.println("      id    = " + toolCall.getId());
                        if (toolCall.getFunction() != null) {
                            System.out.println("      name  = " + toolCall.getFunction().getName());
                            System.out.println("      args  = " + toolCall.getFunction().getArguments());
                        }
                    }
                }
            }
        }

        if (response.getUsage() != null) {
            System.out.println("Usage: prompt=" + response.getUsage().getPromptTokens()
                    + " completion=" + response.getUsage().getCompletionTokens()
                    + " total=" + response.getUsage().getTotalTokens());
        }
        System.out.println(SEP);
    }

    /**
     * 仅打印流式 delta 的内容部分（content），适合连续流式输出。
     */
    public static void printStreamDeltaContent(OpenAiStreamCompletionResponse response) {
        response.getChoices().forEach(choice -> {
            if (choice.getDelta() == null) {
                return;
            }
            String content = choice.getDelta().getContent();
            if (content != null && !content.isBlank()) {
                System.out.print(content);
            }
        });
    }

    /**
     * 打印流式 delta 的内容 + 思考 + 工具调用，适合展示完整流式过程。
     */
    public static void printStreamDeltaAll(OpenAiStreamCompletionResponse response) {
        response.getChoices().forEach(choice -> {
            if (choice.getDelta() == null) {
                return;
            }

            StreamDelta delta = choice.getDelta();

            if (delta.getReasoningContent() != null && !delta.getReasoningContent().isBlank()) {
                System.out.print(delta.getReasoningContent());
            }

            if (delta.getContent() != null && !delta.getContent().isBlank()) {
                System.out.print(delta.getContent());
            }

            if (delta.getToolCalls() != null && !delta.getToolCalls().isEmpty()) {
                for (ToolCall toolCall : delta.getToolCalls()) {
                    System.out.println();
                    System.out.println("[tool_call delta]");
                    System.out.println("  index = " + toolCall.getIndex());
                    System.out.println("  id    = " + toolCall.getId());
                    if (toolCall.getFunction() != null) {
                        System.out.println("  name  = " + toolCall.getFunction().getName());
                        System.out.println("  args  = " + toolCall.getFunction().getArguments());
                    }
                }
            }

            if (choice.getFinishReason() != null) {
                System.out.println();
                System.out.println("[finish_reason] " + choice.getFinishReason());
            }
        });
    }
}
