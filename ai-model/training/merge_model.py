from unsloth import FastLanguageModel

LORA_MODEL = "../models/finetuned/vidyanova-qwen-1.5b"
OUTPUT_DIR = "../models/finetuned/vidyanova-qwen-1.5b-merged"

model, tokenizer = FastLanguageModel.from_pretrained(
    model_name=LORA_MODEL,
    max_seq_length=2048,
    load_in_4bit=False
)

model = model.merge_and_unload()

model.save_pretrained(
    OUTPUT_DIR,
    safe_serialization=True
)

tokenizer.save_pretrained(OUTPUT_DIR)

print("Model merge completed.")
print(f"Merged model saved to: {OUTPUT_DIR}")