from datasets import load_dataset
from config import DATASET_PATH


def load_training_dataset():
    dataset = load_dataset(
        "json",
        data_files=DATASET_PATH,
        split="train"
    )

    return dataset


def format_example(example):
    question = example["question"]
    answer = example["answer"]

    return {
        "text": (
            "<|im_start|>system\n"
            "You are ViyaAI, an offline AI tutor for Class 1 to Class 10. "
            "Stay aligned with the student's curriculum. "
            "Explain according to the requested teaching style. "
            "Use simple and age-appropriate language. "
            "Do not answer unrelated or unsafe questions."
            "<|im_end|>\n"
            "<|im_start|>user\n"
            f"{question}"
            "<|im_end|>\n"
            "<|im_start|>assistant\n"
            f"{answer}"
            "<|im_end|>"
        )
    }


def prepare_dataset():
    dataset = load_training_dataset()

    dataset = dataset.map(
        format_example,
        remove_columns=dataset.column_names
    )

    return dataset