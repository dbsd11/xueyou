FROM --platform=amd64 docker-0.unsee.tech/library/python:3.11.12-bullseye

RUN pip install -Uq pip && pip uninstall -q -y optimum optimum-intel 
RUN pip install --pre -Uq "openvino>=2024.2.0" openvino-tokenizers[transformers] --extra-index-url https://storage.openvinotoolkit.org/simple/wheels/nightly 
RUN pip install -q --extra-index-url https://download.pytorch.org/whl/cpu \
"git+https://github.com/huggingface/optimum-intel.git" \
"nncf==2.14.1" \
"torch>=2.1" \
"datasets" \
"accelerate" \
"gradio>=4.19" \
"huggingface-hub>=0.26.5" \
 "einops" "transformers>=4.43.1" "transformers_stream_generator" "tiktoken" "bitsandbytes"
RUN pip install -q "ipywidgets"

 WORKDIR /app
 COPY . /app/

 EXPOSE 8080
 CMD [ "python", "llm-chatbot-run.py" ]