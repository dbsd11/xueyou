from typing import Callable, Literal
import gradio as gr

def make_demo(run_fn: Callable, stop_fn: Callable, title: str = "OpenVINO Chatbot", language: Literal["English", "Chinese", "Japanese"] = "English"):

    from pages.fingerprint import JS
    
    with gr.Blocks(
        theme=gr.themes.Soft(),
        css=".disclaimer {font-variant-caps: all-small-caps;}",
        js=JS
    ) as demo:
        from pages.index import createIndexPage
        createIndexPage()
    with demo.route("UserInfo", "/info"):
        from pages.userinfo import createUserInfoPage
        createUserInfoPage()
    with demo.route("Chat", "/chat"):
        from pages.chat import createChatPage
        createChatPage(run_fn, stop_fn, title, language)
    with demo.route("Study", "/study"):
        from pages.study import createStudyPage
        createStudyPage()
    return demo

