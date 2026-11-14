# Parakeet 0.6B v3 int8 for sherpa-onnx

Z Board downloads the `sherpa-onnx-nemo-parakeet-tdt-0.6b-v3-int8` package published by the sherpa-onnx project. NVIDIA trained the original Parakeet TDT 0.6B v3 weights; sherpa-onnx exported the original NeMo checkpoint to ONNX.

The package contains one offline transducer (encoder, decoder, joiner, and tokens) for 16 kHz mono audio in 25 languages: Bulgarian, Croatian, Czech, Danish, Dutch, English, Estonian, Finnish, French, German, Greek, Hungarian, Italian, Latvian, Lithuanian, Maltese, Polish, Portuguese, Romanian, Slovak, Slovenian, Spanish, Swedish, Russian, and Ukrainian.

## Attribution and licenses

The weights are NVIDIA's `parakeet-tdt-0.6b-v3`, trained by NVIDIA and licensed under Creative Commons Attribution 4.0 International (CC BY 4.0):

https://creativecommons.org/licenses/by/4.0/

The ONNX export was produced and published by the sherpa-onnx project. sherpa-onnx code is licensed under Apache License 2.0:

https://github.com/k2-fsa/sherpa-onnx/blob/master/LICENSE

NVIDIA does not endorse Z Board or this package.

## Source

Model package:

https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models

Original model:

https://huggingface.co/nvidia/parakeet-tdt-0.6b-v3

The unmodified upstream package README is also retained with the downloadable model files.

## Limitations

This model transcribes speech; it does not identify the spoken language. Long audio should be split at silences. Very quiet input is affected by an upstream frontend issue. Performance and accuracy depend on the device, audio, decoding mode, and dictionary configuration; Z Board does not claim that model weights alone reproduce results from another app.
