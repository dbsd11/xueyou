import gradio as gr

def make_demo():

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
        createChatPage()
    with demo.route("Study", "/study"):
        from pages.study import createStudyPage
        createStudyPage()
    with demo.route("Assistant", "/assistant"):
        from pages.assistant import createAssistantPage
        createAssistantPage()
    return demo

