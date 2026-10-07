import json
import os
import time
from pathlib import Path

from transformers import AutoTokenizer, AutoModelForCausalLM
import torch


MODEL_PATH = "../models/finetuned/vidyanova-qwen-1.5b"
TEST_FILE = "test_questions.json"
RESULT_FILE = "results/evaluation_results.json"

MAX_NEW_TOKENS = 256


def load_test_questions():
    with open(TEST_FILE, "r", encoding="utf-8") as file:
        return json.load(file)


def load_model():
    tokenizer = AutoTokenizer.from_pretrained(MODEL_PATH)

    model = AutoModelForCausalLM.from_pretrained(
        MODEL_PATH,
        torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32,
        device_map="auto"
    )

    model.eval()

    return tokenizer, model


def generate_answer(tokenizer, model, question):
    prompt = (
        "You are ViyaAI, an offline AI tutor for Class 1 to Class 10.\n"
        "Stay within the student's curriculum.\n"
        "Explain step by step when appropriate.\n\n"
        f"Student question: {question}\n"
        "ViyaAI:"
    )

    inputs = tokenizer(
        prompt,
        return_tensors="pt"
    )

    device = next(model.parameters()).device

    inputs = {
        key: value.to(device)
        for key, value in inputs.items()
    }

    start_time = time.perf_counter()

    with torch.no_grad():
        output = model.generate(
            **inputs,
            max_new_tokens=MAX_NEW_TOKENS,
            do_sample=False,
            pad_token_id=tokenizer.eos_token_id
        )

    end_time = time.perf_counter()

    generated_tokens = output[0][inputs["input_ids"].shape[1]:]

    answer = tokenizer.decode(
        generated_tokens,
        skip_special_tokens=True
    ).strip()

    elapsed = end_time - start_time

    tokens_generated = len(generated_tokens)

    tokens_per_second = (
        tokens_generated / elapsed
        if elapsed > 0
        else 0
    )

    return answer, elapsed, tokens_per_second


def evaluate():
    questions = load_test_questions()

    tokenizer, model = load_model()

    results = []

    for item in questions:
        question = item["question"]

        answer, elapsed, tokens_per_second = generate_answer(
            tokenizer,
            model,
            question
        )

        results.append({
            "id": item.get("id"),
            "class": item.get("class"),
            "subject": item.get("subject"),
            "topic": item.get("topic"),
            "question": question,
            "expected_answer": item.get("expected_answer"),
            "generated_answer": answer,
            "generation_time_seconds": round(elapsed, 3),
            "tokens_per_second": round(tokens_per_second, 2)
        })

    output_path = Path(RESULT_FILE)

    output_path.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    with open(output_path, "w", encoding="utf-8") as file:
        json.dump(
            results,
            file,
            indent=2,
            ensure_ascii=False
        )

    print("Evaluation completed.")
    print(f"Questions evaluated: {len(results)}")
    print(f"Results saved to: {RESULT_FILE}")


if __name__ == "__main__":
    evaluate()