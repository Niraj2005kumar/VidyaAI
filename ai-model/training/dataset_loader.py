STYLE_INSTRUCTIONS = {
    "simple": "Explain simply and concisely.",
    "detailed": "Give a detailed conceptual explanation.",
    "step_by_step": "Explain step by step.",
    "example": "Use relatable real-life examples.",
    "exam_ready": "Give an exam-ready answer with key points.",
    "basic": "Explain the basic foundational concepts.",
}

LANGUAGES = {
    "english": "English",
    "hindi": "Hindi",
    "hinglish": "Hinglish",
}


def _build_response(example):
    response = str(example.get("response", "")).strip()
    if response:
        return response

    # purane format ke liye fallback
    answer = str(example.get("answer", "")).strip()
    explanation = str(example.get("explanation", "")).strip()
    steps = example.get("steps", [])
    parts = [answer, explanation]
    if example.get("teaching_style") == "step_by_step" and isinstance(steps, list):
        parts.append("\n".join(f"{i + 1}. {s}" for i, s in enumerate(steps)))
    return "\n\n".join(p for p in parts if p)


def format_example(example):
    cls = example.get("class", "")
    subject = str(example.get("subject", "")).strip()
    chapter = str(example.get("chapter", "")).strip()
    topic = str(example.get("topic", "")).strip()

    language = LANGUAGES.get(
        str(example.get("language", "english")).strip().lower(), "English"
    )
    style = STYLE_INSTRUCTIONS.get(
        str(example.get("teaching_style", "simple")).strip().lower(),
        STYLE_INSTRUCTIONS["simple"],
    )

    chapter_line = f"Chapter: {chapter}." if chapter else ""
    topic_line = f"Topic: {topic}." if topic else ""

    system_prompt = (
        "You are ViyaAI, an offline educational AI tutor.\n"
        f"Teach students of Class {cls}.\n"
        f"Subject: {subject}.\n"
        f"{chapter_line}\n"
        f"{topic_line}\n"
        "\n"
        f"Respond in {language}.\n"
        f"{style}\n"
        "\n"
        "Use age-appropriate language.\n"
        "Explain concepts accurately.\n"
        "Use examples when helpful.\n"
        "Do not invent textbook facts.\n"
        "If information is insufficient, clearly say so."
    )

    question = str(example.get("question", "")).strip()
    response = _build_response(example)

    text = (
        "<|im_start|>system\n" + system_prompt + "<|im_end|>\n"
        "<|im_start|>user\n" + question + "<|im_end|>\n"
        "<|im_start|>assistant\n" + response + "<|im_end|>"
    )
    return {"text": text}