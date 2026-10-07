from unsloth import FastLanguageModel
from trl import SFTTrainer
from transformers import TrainingArguments

from config import (
    MODEL_NAME,
    OUTPUT_DIR,
    MAX_SEQ_LENGTH,
    LORA_R,
    LORA_ALPHA,
    LORA_DROPOUT,
    BATCH_SIZE,
    GRADIENT_ACCUMULATION_STEPS,
    LEARNING_RATE,
    NUM_EPOCHS,
    WARMUP_STEPS,
    SAVE_STEPS,
    LOGGING_STEPS
)

from dataset_loader import prepare_dataset


model, tokenizer = FastLanguageModel.from_pretrained(
    model_name=MODEL_NAME,
    max_seq_length=MAX_SEQ_LENGTH,
    load_in_4bit=True,
    dtype=None
)

model = FastLanguageModel.get_peft_model(
    model,
    r=LORA_R,
    lora_alpha=LORA_ALPHA,
    lora_dropout=LORA_DROPOUT,
    bias="none",
    use_gradient_checkpointing="unsloth",
    random_state=3407
)

dataset = prepare_dataset()

trainer = SFTTrainer(
    model=model,
    tokenizer=tokenizer,
    train_dataset=dataset,
    dataset_text_field="text",
    max_seq_length=MAX_SEQ_LENGTH,
    packing=False,
    args=TrainingArguments(
        output_dir=OUTPUT_DIR,
        per_device_train_batch_size=BATCH_SIZE,
        gradient_accumulation_steps=GRADIENT_ACCUMULATION_STEPS,
        learning_rate=LEARNING_RATE,
        num_train_epochs=NUM_EPOCHS,
        warmup_steps=WARMUP_STEPS,
        logging_steps=LOGGING_STEPS,
        save_steps=SAVE_STEPS,
        fp16=True,
        optim="adamw_8bit",
        report_to="none"
    )
)

trainer.train()

model.save_pretrained(OUTPUT_DIR)
tokenizer.save_pretrained(OUTPUT_DIR)

print("ViyaAI fine-tuning completed.")
print(f"Model saved to: {OUTPUT_DIR}")