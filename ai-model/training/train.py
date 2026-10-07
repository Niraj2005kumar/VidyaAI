import os

from datasets import load_dataset
from unsloth import FastLanguageModel
from trl import SFTTrainer
from transformers import TrainingArguments

from config import (
    MODEL_NAME,
    DATASET_PATH,
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
    LOGGING_STEPS,
)
from dataset_loader import format_example


def main():

    print("Loading Qwen2.5-1.5B-Instruct...")

    model, tokenizer = FastLanguageModel.from_pretrained(
        model_name=MODEL_NAME,
        max_seq_length=MAX_SEQ_LENGTH,
        load_in_4bit=True,
        dtype=None,
    )

    print("Applying LoRA...")

    model = FastLanguageModel.get_peft_model(
        model,
        r=LORA_R,
        lora_alpha=LORA_ALPHA,
        lora_dropout=LORA_DROPOUT,
        target_modules=[
            "q_proj",
            "k_proj",
            "v_proj",
            "o_proj",
            "gate_proj",
            "up_proj",
            "down_proj",
        ],
        bias="none",
        use_gradient_checkpointing="unsloth",
        random_state=3407,
    )

    print("Loading dataset...")

    dataset = load_dataset(
        "json",
        data_files=DATASET_PATH,
        split="train",
    )

    dataset = dataset.map(
        format_example,
        remove_columns=dataset.column_names,
    )

    print(f"Training examples: {len(dataset)}")

    training_args = TrainingArguments(
        output_dir=OUTPUT_DIR,
        per_device_train_batch_size=BATCH_SIZE,
        gradient_accumulation_steps=GRADIENT_ACCUMULATION_STEPS,
        learning_rate=LEARNING_RATE,
        num_train_epochs=NUM_EPOCHS,
        warmup_steps=WARMUP_STEPS,
        save_steps=SAVE_STEPS,
        logging_steps=LOGGING_STEPS,
        fp16=True,
        optim="adamw_8bit",
        weight_decay=0.01,
        lr_scheduler_type="linear",
        seed=3407,
        report_to="none",
    )

    trainer = SFTTrainer(
        model=model,
        tokenizer=tokenizer,
        train_dataset=dataset,
        dataset_text_field="text",
        max_seq_length=MAX_SEQ_LENGTH,
        packing=False,
        args=training_args,
    )

    print("Starting training...")

    trainer.train()

    print("Saving fine-tuned model...")

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    model.save_pretrained(OUTPUT_DIR)
    tokenizer.save_pretrained(OUTPUT_DIR)

    print("Training completed successfully.")
    print(f"Model saved to: {OUTPUT_DIR}")


if __name__ == "__main__":
    main()