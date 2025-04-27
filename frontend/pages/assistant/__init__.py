import gradio as gr
from numpy import equal, var
import pandas as pd
import json
from pathlib import Path

def uploadExpenseFile(files):
    import requests
    try:
        # 构建FormData对象
        form_data = {}
        form_data["files"] = []
        for file in files:
            file_name = Path(file.name).name
            file_obj = open(str(file), "rb")
            form_data["files"].append(("files", (file_name, file_obj, "application/octet-stream")))

        # 发送请求
        response = requests.post(
            f"http://localhost:18888/api/file/parse-expense-file",
            files=form_data["files"]
        )
        response.raise_for_status()
        response_data = json.dumps(response.json()["data"])
        print("upload expense file success", files, response_data)
        file_paths = [file.name for file in files]
        return file_paths, response_data
    except requests.exceptions.RequestException as e:
        print(f"上传费用支出文件失败: {str(e)}")
        return []

def create_css():
    gr.HTML("""
        <style>
        .half-width {
            margin: 0 auto;
            max-width: 50% !important;
        }
        </style>
    """)

def createAssistantPage():
    with gr.Blocks() as assistantPage:
        create_css()

        # 添加 JavaScript 检查 cookie 并获取用户信息
        assistantPage.load(
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
                    fetch(`http://localhost:18888/api/account-records/future-records`, {
                        method: "GET",
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': visitorId
                        },
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code === 0) {
                            document.querySelector('#future_expense textarea').value = JSON.stringify(data.data);
                            document.querySelector('#future_expense textarea').dispatchEvent(new Event('input', {
                                bubbles: true,
                                cancelable: true,
                            }));
                        }
                    });
                }
            }
        """)
    
        gr.Markdown(f"""<h1><center>WUYOU study friends</center></h1>""")
        with gr.Row():
            with gr.Column(elem_classes="half-width"):
                gr.Markdown("## 预计支出项目")
                future_expense_text = gr.Textbox(label="预计支出项目", visible=False, elem_id="future_expense")
                @gr.render(inputs=future_expense_text, triggers=[future_expense_text.change])
                def renderFutureExpense(future_expense_text):
                    if len(future_expense_text) == 0:
                        return
                    future_expense_data = json.loads(future_expense_text)
                    if len(future_expense_data) > 0:
                        for item in future_expense_data:
                            with gr.Row(equal_height = True, variant="compact"):
                                with gr.Column(scale=2):
                                    with gr.Row():
                                        gr.Textbox(value=f"未来预计时间: {item['createTime']}   事项: {item['details']}", label="预计支出项目", show_label=False, container=False, show_copy_button=True)
                                    with gr.Row():
                                        gr.Textbox(value=f"预计支出: {item['amount']}", label="支出", show_label=False, container=False, show_copy_button=True)
                                with gr.Column(scale=1):
                                    if item["aiSuggestion"]:
                                        line_count = len(item["aiSuggestion"].split("\n"))
                                        gr.Textbox(value=item["aiSuggestion"], label="学友建议", lines=line_count, show_label=True, interactive=True, show_copy_button=True, submit_btn=True) 

        with gr.Row():    
            with gr.Column(elem_classes="half-width"):
                gr.Markdown("## 支出记账")
                with gr.Row():
                    period = gr.Dropdown(
                        choices=[["最近1月", "1month"], ["最近3月", "3month"], ["最近一年", "1year"]],
                        label="时间范围"
                    )
                    queryButton = gr.Button(value="查询", variant="primary", elem_id="queryButton")
                expense_text = gr.Textbox(label="支出项目", visible=False, elem_id="expense")
                @gr.render(inputs=expense_text, triggers=[expense_text.change])
                def renderExpense(expense_text):
                    if len(expense_text) == 0:
                        return
                    expense_data = json.loads(expense_text)
                    if len(expense_data["content"]) > 0:
                        with gr.Row(equal_height = True, variant="compact"):
                            gr.Slider(1, expense_data["totalPages"], step=1, value=expense_data["number"] + 1, label="page", info="Choose Page", elem_id="page", interactive=True)
                        expenseList = []
                        for item in expense_data["content"]:
                            expenseList.append([f"时间: {item['createTime']}   事项: {item['details']}   支出: {item['amount']}", item["id"]])
                        gr.CheckboxGroup(choices=expenseList, value=[], type="value", show_label=False, container=False, elem_id="expense_check_list")
                        deleteButton = gr.Button(value="删除选择项", variant="secondary", elem_id="deleteButton")
                        deleteButton.click(
                            fn=None,
                            inputs=[],
                            outputs=[],
                            queue=True,
                            trigger_mode="once",
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
                                    selectedCheckBoxInputs = document.querySelectorAll('#expense_check_list .selected input')
                                    selectedCheckBoxValues = []
                                    for (let i = 0; i < selectedCheckBoxInputs.length; i++) {
                                        selectedCheckBoxValues.push(selectedCheckBoxInputs[i].name)
                                    }
                                    if (selectedCheckBoxValues.length == 0) {
                                        return
                                    }

                                    // 删除支出记录
                                    fetch(`http://localhost:18888/api/account-records/delete`, {
                                        method: "POST",
                                        headers: {
                                            'Content-Type': 'application/json',
                                            'fp': visitorId
                                        },
                                        body: JSON.stringify(selectedCheckBoxValues),
                                        credentials: 'include'
                                    })
                                    .then(response => response.json())
                                    .then(data => {
                                        if (data.code === 0) {
                                            alert("delete success", data);
                                            document.querySelector('#queryButton').click();
                                        }
                                    });
                                }

                            }
                            """
                        )

        with gr.Row():
            expense_stat_text = gr.Textbox(label="支出统计", visible=False, elem_id="expense_stat")
            @gr.render(inputs=expense_stat_text, triggers=[expense_stat_text.change])
            def renderExpenseStat(expense_text):
                if len(expense_text) == 0:
                    return
                expense_data = json.loads(expense_text)

                demo_data = pd.DataFrame(
                    {
                        "type": ["期望月均消费", "实际月均消费"],
                        "spend": [expense_data["expectMonthlySpend"], expense_data["averageMonthlyExpense"]],
                    }
                )
                with gr.Column(elem_classes="half-width"):
                    gr.Markdown("### 支出统计")
                    gr.BarPlot(
                        demo_data,
                        x="type",
                        y="spend",
                        color="type",
                        x_title = "类型",
                        y_title = "月均消费",
                        sort="-x",
                        tooltip="axis",
                    )
        with gr.Row():
            with gr.Column(elem_classes="half-width"):
                gr.Markdown("## 添加记账")
                with gr.Row():
                    add_btn = gr.UploadButton(label="上传历史支出记账图片", file_types=["image", "text"], variant="secondary", type="filepath", file_count="multiple")
                with gr.Row():
                    uploadFile = gr.File(label="记账记录文件")
                    expenseFileParseText = gr.Textbox(label="记账记录文件解析结果", visible=False, elem_id="expense_file_parse_text")
                add_btn.upload(fn=uploadExpenseFile, inputs=[add_btn], outputs=[uploadFile, expenseFileParseText], queue=True, trigger_mode="once")
                with gr.Row():
                    submitNewExpense = gr.Textbox(label="新增支出记账", show_label=True, submit_btn=True, placeholder="输入具体的支出项目", elem_id="submitNewExpense")

        queryButton.click(
            fn=None,
            inputs=[period],
            outputs=[],
            queue=True,
            trigger_mode="once",
            js="""
            async (period) => {
                console.info("query spend with period ", period);
                function getCookie(name) {
                    const value = `; ${document.cookie}`;
                    const parts = value.split(`; ${name}=`);
                    if (parts.length === 2) return parts.pop().split(';').shift();
                    return null;
                }
                function formatDate(date) {
                    let year = date.getFullYear();
                    let month = (date.getMonth() + 1).toString().padStart(2, '0'); // 月份是从0开始的
                    let day = date.getDate().toString().padStart(2, '0');
                    let hours = date.getHours().toString().padStart(2, '0');
                    let minutes = date.getMinutes().toString().padStart(2, '0');
                    let seconds = date.getSeconds().toString().padStart(2, '0');
                    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
                }
                const token = getCookie('token');
                const visitorId = localStorage.getItem('visitorId');
                if (token && visitorId) {
                    startTime = formatDate(new Date(new Date().getTime() - (period == '1month' ? 30 : period == '3month' ? 90 : 365) * 24 * 60 * 60 * 1000)); // 1 month ago
                    endTime = formatDate(new Date());
                    page = document.querySelector('#page input') ? document.querySelector('#page input').value : 1;

                    // 获取支出记录
                    fetch(`http://localhost:18888/api/account-records/time-range?startTime=${startTime}&endTime=${endTime}&page=${page}`, {
                        method: "GET",
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': visitorId
                        },
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code === 0) {
                            document.querySelector('#expense textarea').value = JSON.stringify(data.data);
                            document.querySelector('#expense textarea').dispatchEvent(new Event('input', {
                                bubbles: true,
                                cancelable: true,
                            }));
                        }
                    });

                    // 获取月均消费
                    fetch(`http://localhost:18888/api/account-records/average-monthly-expense?startTime=${startTime}&endTime=${endTime}`, {
                        method: "GET",
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': visitorId
                        },
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code === 0) {
                            document.querySelector('#expense_stat textarea').value = JSON.stringify(data.data);
                            document.querySelector('#expense_stat textarea').dispatchEvent(new Event('input', {
                                bubbles: true,
                                cancelable: true,
                            }));
                        }
                    });
                }
            }
            """
        )

        expenseFileParseText.change(
            fn=None,
            inputs=[expenseFileParseText],
            outputs=[],
            queue=True,
            trigger_mode="once",
            js="""
            async (expenseFileParseText) => {
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
                    fetch('http://localhost:18888/api/account-records', {
                        method: "POST",
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': visitorId
                        },
                        body: expenseFileParseText,
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code === 0) {
                            console.info("upload expense file success", data);
                            document.querySelector('#queryButton').click();
                        }
                    });
                }
            }
            """
        )

        submitNewExpense.submit(
            fn=None,
            inputs=[submitNewExpense],
            outputs=[],
            queue=True,
            trigger_mode="once",
            js="""
            async (submitNewExpense) => {
                console.info("submit new expense ", submitNewExpense);
                function getCookie(name) {
                    const value = `; ${document.cookie}`;
                    const parts = value.split(`; ${name}=`);
                    if (parts.length === 2) return parts.pop().split(';').shift();
                    return null;
                }
                const token = getCookie('token');
                const visitorId = localStorage.getItem('visitorId');
                if (token && visitorId) {
                    const btn = document.querySelector('#submitNewExpense');
                    if (!btn) return;
                    
                    btn.disabled = true;
                    btn.style.opacity = '0.5';

                    // 获取用户信息并填充表单
                    fetch('http://localhost:18888/api/account-records/parse-submit', {
                        method: "POST",
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': visitorId
                        },
                        body: JSON.stringify({
                            "details": submitNewExpense
                        }),
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        btn.disabled = false;
                        btn.style.opacity = '1';
                        if (data.code === 0) {
                            console.info("submit expense success", data);
                        } else {
                            console.info("submit expense failed", data); 
                        }
                    });
                }
            }
            """
        )
    return assistantPage