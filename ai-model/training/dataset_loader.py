def format_example(example):
    question = str(example.get("question", "")).strip()
    answer = str(example.get("answer", "")).strip()
    explanation = str(example.get("explanation", "")).strip()
    steps = example.get("steps", [])

    if isinstance(steps, list):
        steps_text = "\n".join(
            f"{i + 1}. {step}"
            for i, step in enumerate(steps)
        )
    else:
        steps_text = str(steps).strip()

    language = str(
        example.get("language", "english")
    ).strip()

    teaching_style = str(
        example.get("teaching_style", "simple")
    ).strip()

    subject = str(
        example.get("subject", "")
    ).strip()

    chapter = str(
        example.get("chapter", "")
    ).strip()

    topic = str(
        example.get("topic", "")
    ).strip()

    text = f"""<|im_start|>system
You are ViyaAI, an offline AI tutor for school students.
Stay within the student's curriculum.
Explain concepts clearly and safely.
Adapt the explanation to the requested teaching style and language.
<|im_end|>
<|im_start|>user
Class: {example.get("class", "")}
Subject: {subject}
Chapter: {chapter}
Topic: {topic}
Language: {language}
Teaching Style: {teaching_style}

Question:
{question}
<|im_end|>
<|im_start|>assistant
Answer:
{answer}

Explanation:
{explanation}

Steps:
{steps_text}
<|im_end|>"""

    return {
        "text": text
    }