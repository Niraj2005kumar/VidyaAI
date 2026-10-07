#!/bin/bash

set -e

MODEL_DIR="../models/finetuned/vidyanova-qwen-1.5b"
MERGED_DIR="../models/finetuned/vidyanova-qwen-1.5b-merged"
OUTPUT_DIR="../models/quantized"

mkdir -p "$OUTPUT_DIR"

python ../llama.cpp/convert_hf_to_gguf.py \
    "$MERGED_DIR" \
    --outfile "$OUTPUT_DIR/vidyanova-qwen-1.5b-f16.gguf" \
    --outtype f16

../llama.cpp/build/bin/llama-quantize \
    "$OUTPUT_DIR/vidyanova-qwen-1.5b-f16.gguf" \
    "$OUTPUT_DIR/vidyanova-qwen-1.5b-q4_k_m.gguf" \
    Q4_K_M

echo "Quantization completed."
echo "Output: $OUTPUT_DIR/vidyanova-qwen-1.5b-q4_k_m.gguf"