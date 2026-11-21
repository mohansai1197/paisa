# Model and reasoning-level guide

[Back to the file directory](../README.md)

**Preference:** use Medium when sufficient and High when a task needs deeper reasoning. These are reasoning levels; selecting a different model is a separate choice.

**Capability limit in this chat:** no direct tool is available to the assistant for automatically changing this active chat's model or reasoning setting. No model, global configuration, or reasoning-level setting has been changed. A written preference does not enable automatic switching.

## Suggested workflow

These are our practical recommendations, not guaranteed outcomes or official task-by-task requirements.

| Reasoning level | Suggested use |
| --- | --- |
| Medium | Routine React components and styling, straightforward CRUD endpoints, documentation, pitch edits, and well-understood fixes |
| High | Architecture tradeoffs, unfamiliar failures across frontend/backend/database boundaries, complex business rules, difficult debugging, and security-sensitive logic |

Start with Medium for routine work. Consider High when the cause remains unclear after a focused investigation or the task requires comparing several consequential approaches. Return to Medium once the difficult decision or diagnosis is resolved. Higher effort does not guarantee correctness; verify the implementation at either level.

The assistant can recommend a switch when useful. The user must apply it through the app's available model/reasoning controls; exact levels depend on the selected model and client. Do not claim that a recommendation has changed the actual setting.

## Recommendation after completed work

After each completed task or meaningful execution milestone, end the user-facing update with **Next effort: Medium/High — [brief reason for the next task].** Recommend one level based on the next known task. When the next task is unknown, recommend Medium as the routine starting point. This is advice for the next execution, not a claim about the current setting or an automatic change. Keep internal tool calls batched normally; a separate user-facing recommendation after every tool call is unnecessary.

## Official reference

The [OpenAI configuration reference](https://learn.chatgpt.com/docs/config-file/config-reference) documents `model_reasoning_effort` and notes that available levels depend on the model and client. Checked 2026-11-21.
