import gradio as gr

def sendPhoneCode(phone):
    print(f"触发发送验证码到{phone}")
    # 请求 http://localhost:18888/api/auth/sendCode
    import requests
    try:
        response = requests.post(
            f"http://localhost:18888/api/auth/sendCode?phone={phone}",
            headers={
                'Content-Type': 'application/json'
            }
        )
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        print(f"发送验证码请求失败: {str(e)}")

def create_css():
    gr.HTML("""
        <style>
        .half-width {
            margin: 0 auto;
            max-width: 50% !important;
        }
        .success-text {
            color: #4CAF50;
            font-weight: bold;
        }
        .error-text {
            color: #F44336;
            font-weight: bold;
        }
        </style>
    """)

def createUserInfoPage():
    with gr.Blocks() as userInfoPage:
        create_css()

        # 添加 JavaScript 检查 cookie 并获取用户信息
        userInfoPage.load(
            fn=None,
            inputs=None,
            outputs=None,
            js="""
            async () => {
                function getCookie(name) {
                    const value = `; ${document.cookie}`;
                    const parts = value.split(`; ${name}=`);
                    if (parts.length === 2) return parts.pop().split(';').shift();
                    return null;
                }
                const token = getCookie('token');
                const visitorId = localStorage.getItem('visitorId');
                if (token && visitorId) {
                    // 获取用户信息并填充表单
                    fetch('http://localhost:18888/api/students/current', {
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': visitorId
                        },
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code === 0) {
                            // 隐藏登录相关元素
                            document.querySelector('#login-section').style.display = 'none';
                            // 显示用户信息
                            document.querySelector('#userinfo-section').style.display = 'block';
                            // 填充用户信息
                            Object.keys(data.data).forEach(key => {
                                const input = document.querySelector(`#${key}-input textarea`);
                                if (input) input.value = data.data[key];
                            });
                        }
                    });
                }
            }
        """)

        gr.Markdown(f"""<h1><center>WUYOU study friends</center></h1>""")
        gr.Markdown(f"""<h3><center>UserInfo</center></h3>""")

        # 登录部分
        with gr.Column(elem_classes="half-width", elem_id="login-section"):
            with gr.Row():
                    phone = gr.Textbox(label="phone", placeholder="手机号", show_label=False, container=False, elem_id="phone-input")
            with gr.Row():
                with gr.Column():
                    code = gr.Textbox(label="code", placeholder="验证码", show_label=False, container=False, elem_id="code-input")
                with gr.Column():
                    send_button = gr.Button("发送验证码", elem_id="sendCodeButton")
            with gr.Row():
                with gr.Column(scale=20):
                    login_button = gr.Button("登录", elem_id="loginButton")
                with gr.Column(scale=1):
                    login_result = gr.HTML("")

        # 用户信息部分
        with gr.Row(elem_classes="half-width", elem_id="userinfo-section", visible=False):
            with gr.Column(visible=False):
                id = gr.Textbox(label="id", elem_id="id-input")
            with gr.Column():
                gender = gr.Textbox(label="gender", placeholder="性别", show_label=True, container=True, elem_id="gender-input")
            with gr.Column():
                age = gr.Textbox(label="age", placeholder="年龄", show_label=True, container=True, elem_id="age-input")
            with gr.Column():
                college = gr.Textbox(label="college", placeholder="院校", show_label=True, container=True, elem_id="college-input")
            with gr.Column():
                major = gr.Textbox(label="major", placeholder="专业", show_label=True, container=True, elem_id="major-input")
            with gr.Column():
                grade = gr.Textbox(label="grade", placeholder="年级", show_label=True, container=True, elem_id="grade-input")
            with gr.Column():
                grade = gr.Textbox(label="expectMonthlySpend", placeholder="预计每月支出", show_label=True, container=True, elem_id="expectMonthlySpend-input")
            with gr.Column():
                save_info_button = gr.Button(value="保存信息", elem_id="saveInfoButton")
                save_info_result = gr.HTML("")

        submit_event = send_button.click(
            fn=sendPhoneCode,
            inputs=[phone],
            outputs=None,
            queue=True,
            trigger_mode="once",
        )

        login_event = login_button.click(
            fn=None,
            inputs=[phone, code],
            outputs=[login_result],
            queue=True,
            trigger_mode="once",
            js="""
            async () => {
                const phone = document.querySelector('#phone-input textarea').value;
                const code = document.querySelector('#code-input textarea').value;  
                console.debug("call login api with phone and code", phone, code);

                // 请求 http://localhost:18888/api/auth/verify
                const response = await fetch(`http://localhost:18888/api/auth/verify?phone=${phone}&code=${code}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'fp': localStorage.getItem('visitorId')
                    },
                    credentials: 'include'
                });
                document.location.reload();
            }
            """
        )

        save_info_button.click(
            fn=None,
            inputs=[id, gender, age, college, major, grade],
            outputs=[save_info_result],
            queue=True,
            trigger_mode="once",
            js="""
            async () => {
                const id = document.querySelector('#id-input textarea').value;
                const gender = document.querySelector('#gender-input textarea').value;
                const age = document.querySelector('#age-input textarea').value;
                const college = document.querySelector('#college-input textarea').value;  
                const major = document.querySelector('#major-input textarea').value;  
                const grade = document.querySelector('#grade-input textarea').value;  
                const expectMonthlySpend = document.querySelector('#expectMonthlySpend-input textarea').value;  
                console.debug("call save info api with sex, age, college, major, level, expectMonthlySpend", gender, age, college, major, grade, expectMonthlySpend);

                const visitorId = localStorage.getItem('visitorId');
                if (visitorId) {
                    // 获取用户信息并填充表单
                    fetch('http://localhost:18888/api/students/upsert', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': visitorId
                        },
                        body: JSON.stringify({
                            "id": id,
                            "gender": gender,
                            "age": age,
                            "college": college, 
                            "major": major,
                            "grade": grade,
                            "expectMonthlySpend": expectMonthlySpend
                        }),
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code !== 0) {
                            alert("update failed with code: " + data.code + " msg: " + data.msg);
                        } else {
                            alert("update success!");
                        }
                    });
                }
            }
            """
        )
    return userInfoPage

    