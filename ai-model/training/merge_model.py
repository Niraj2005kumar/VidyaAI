import torch
from peft import AutoPeftModelForCausalLM
from transformers import AutoTokenizer

LORA_MODEL = "../models/finetuned/vidyanova-qwen-0.5b"
OUTPUT_DIR = "../models/merged/vidyanova-qwen-0.5b"

print("Loading model + LoRA adapter...")
model = AutoPeftModelForCausalLM.from_pretrained(
    LORA_MODEL,
    torch_dtype=torch.float32
)

print("Merging...")
model = model.merge_and_unload()

model.save_pretrained(
    OUTPUT_DIR,
    safe_serialization=True
)

tokenizer = AutoTokenizer.from_pretrained(LORA_MODEL)
tokenizer.save_pretrained(OUTPUT_DIR)

print("Model merge completed.")
print(f"Merged model saved to: {OUTPUT_DIR}")