from gradio_helper_wuyou import make_demo

demo = make_demo()

try:
    demo.launch(server_name='0.0.0.0', server_port=8080)
except Exception as e:
    print(e)
    demo.launch(share=True)