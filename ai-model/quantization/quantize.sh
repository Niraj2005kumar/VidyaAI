$ErrorActionPreference = "Stop"

$LLAMA_CPP = "../../android-app/llama.cpp"
$MODEL_INPUT = "../models/finetuned/vidyanova-qwen-1.5b"
$MODEL_MERGED = "../models/merged/vidyanova-qwen-1.5b"
$MODEL_OUTPUT = "../models/quantized/tutor-model.gguf"

Write-Host "Step 1: Converting Hugging Face model to GGUF..."

python "$LLAMA_CPP/convert_hf_to_gguf.py" `
    $MODEL_MERGED `
    --outfile $MODEL_OUTPUT `
    --outtype f16

Write-Host "Step 2: Quantizing GGUF to Q4_K_M..."

$QUANTIZE_EXE = "$LLAMA_CPP/build/bin/llama-quantize.exe"

if (!(Test-Path $QUANTIZE_EXE)) {
    Write-Host "llama-quantize.exe not found."
    Write-Host "Build llama.cpp first."
    exit 1
}

$F16_MODEL = "../models/quantized/tutor-model-f16.gguf"

if (Test-Path $F16_MODEL) {
    Remove-Item $F16_MODEL
}

Rename-Item $MODEL_OUTPUT "tutor-model-f16.gguf"

& $QUANTIZE_EXE `
    "../models/quantized/tutor-model-f16.gguf" `
    $MODEL_OUTPUT `
    Q4_K_M

Write-Host ""
Write-Host "Q4_K_M GGUF created successfully:"
Write-Host $MODEL_OUTPUT