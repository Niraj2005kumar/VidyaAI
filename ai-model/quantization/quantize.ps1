param(
    [string]$QUANT_TYPE = "Q4_K_M"
)

$ErrorActionPreference = "Stop"

$LLAMA_CPP = "../../android-app/llama.cpp"
$MODEL_MERGED = "../models/merged/vidyanova-qwen-0.5b"
$MODEL_OUTPUT = "../models/quantized/tutor-model.gguf"
$F16_MODEL = "../models/quantized/tutor-model-f16.gguf"

New-Item -ItemType Directory -Force -Path "../models/quantized" | Out-Null

Write-Host "Step 1: Converting Hugging Face model to GGUF..."

python "$LLAMA_CPP/convert_hf_to_gguf.py" `
    $MODEL_MERGED `
    --outfile $F16_MODEL `
    --outtype f16

if (!(Test-Path $F16_MODEL)) {
    Write-Host "F16 conversion failed."
    exit 1
}

Write-Host "Step 2: Quantizing GGUF to $QUANT_TYPE..."

$QUANTIZE_EXE = "$LLAMA_CPP/build/bin/llama-quantize.exe"
if (!(Test-Path $QUANTIZE_EXE)) {
    $QUANTIZE_EXE = "$LLAMA_CPP/build/bin/Release/llama-quantize.exe"
}

if (!(Test-Path $QUANTIZE_EXE)) {
    Write-Host "llama-quantize.exe not found."
    Write-Host "Build llama.cpp first."
    exit 1
}

if (Test-Path $MODEL_OUTPUT) {
    Remove-Item $MODEL_OUTPUT
}

& $QUANTIZE_EXE $F16_MODEL $MODEL_OUTPUT $QUANT_TYPE

Write-Host ""
Write-Host "$QUANT_TYPE GGUF created successfully:"
Write-Host $MODEL_OUTPUT