# 前端启动步骤
1. 准备python3.11环境
  conda activate python3-11
2. 安装jupter-notebook, 并启动
  pip install notebook
  python -m notebook
3. 运行llm-chatbot.ipynb
  执行成功完，本地就会出现压缩好的千问0.5b的模型qwen2.5-0.5b-instruct
4. 启动前端项目
  python llm-chatbot-run.py

# 后端启动步骤
1. 编译打包：
   mvn clean install -DskipTests
2. 启动
  java -jar xxxx
