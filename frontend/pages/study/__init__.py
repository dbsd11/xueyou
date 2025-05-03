import gradio as gr
from pages import backend_api

def parseStudyPlanText(studyPlanText):
    import json
    from datetime import datetime

    try:
        # 解析JSON字符串
        plans = json.loads(studyPlanText)
        
        # 按照开始时间排序
        def get_sort_key(plan):
            time = datetime.strptime(plan['eventStartTime'], '%H:%M')
            return (time.hour * 60 + time.minute)
        
        plans.sort(key=get_sort_key)
        
        # 初始化结果数组
        result = []
        
        # 生成表格数据
        for plan in plans:
            weekday = plan['weekday']
            # 创建每个计划的显示内容
            plan_info = f"""
                **{plan['eventContent']}**  
                开始时间: {plan['eventStartTime']}  
                时长: {plan['duration']}
                """.strip()
            
            # 将计划放入对应星期的位置
            row = [''] * 7
            row[weekday - 1] = plan_info
            result.append(row)
        return result

    except Exception as e:
        print("解析学习计划出错:", str(e))
        return [['' for _ in range(7)]]

def parseSuggestionText(suggestionText):
    import json
    suggestions = json.loads(suggestionText)

    # 生成表格数据
    result = []
    for suggestion in suggestions:
        result.append([suggestion])
    return gr.Dataset(samples=result)

def createStudyPage():
    with gr.Blocks() as studyPage:
        gr.Markdown(f"""<h1><center>WUYOU study friends</center></h1>""")
        
        with gr.Column():
            with gr.Row():
                startTime = gr.DateTime(label="开始时间", scale=2, elem_id="startTime")
                gr.Markdown(value="<p><center>时间范围<br/>至</center></p>")
                endTime = gr.DateTime(label="结束时间", scale=2, elem_id="endTime")
                queryButton = gr.Button(value="查询", variant="primary", elem_id="queryButton")
            with gr.Row():
                studyPlanText = gr.Textbox(elem_id="studyPlanText", visible=False)
                studyPlanTextDataFrame = gr.Dataframe(headers=['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'], datatype=['markdown', 'markdown', 'markdown', 'markdown', 'markdown', 'markdown', 'markdown'],
                    col_count=7.0, label="学习计划", show_label=True, type="array", show_fullscreen_button=True)
        with gr.Row():
            submitNewStudyPlan = gr.Textbox(label="变更学习计划", show_label=True, submit_btn=True, placeholder="输入具体变更内容, 可以参考学友的建议哦")
            with gr.Row():
                suggestionText = gr.Textbox(elem_id="suggestionText", visible=False)
                suggestionExampleComponent = gr.Examples(examples=[['']], inputs=submitNewStudyPlan, label="学友建议: ")
        
        queryButton.click(
            fn=None,
            inputs=[startTime, endTime],
            outputs=[studyPlanText],
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
                if (token) {
                    const btn = document.querySelector('#queryButton');
                    if (!btn) return;

                    btn.disabled = true;
                    btn.style.opacity = '0.5';

                    // 隐藏登录相关元素
                    const startTime = document.querySelector('#startTime input').value;
                    const endTime = document.querySelector('#endTime input').value;

                    // 获取学习计划并填充表单
                    fetch(`BASE_URL/api/studyplans?startTime=${startTime}&endTime=${endTime}`, {
                        method: 'GET',
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': localStorage.getItem('visitorId')
                        },
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code == 0) {
                            // 隐藏登录相关元素
                            document.querySelector('#studyPlanText textarea').value = JSON.stringify(data.data);
                            document.querySelector('#studyPlanText textarea').dispatchEvent(new Event('input', {
                                bubbles: true,
                                cancelable: true,
                            }));
                        }
                    });

                    // 获取学习计划建议
                    fetch(`BASE_URL/api/studyplans/suggestion?startTime=${startTime}&endTime=${endTime}`, {
                        method: 'GET',
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': localStorage.getItem('visitorId')
                        },
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        btn.disabled = false;
                        btn.style.opacity = '1';

                        if (data.code == 0) {
                            // 隐藏登录相关元素
                            document.querySelector('#suggestionText textarea').value = JSON.stringify(data.data);
                            document.querySelector('#suggestionText textarea').dispatchEvent(new Event('input', {
                                bubbles: true,
                                cancelable: true,
                            }));
                        }
                    }).catch(error => {
                        btn.disabled = false;
                        btn.style.opacity = '1';
                        console.error('Error:', error);
                    });
                }
            }
        """.replace("BASE_URL", backend_api.BACKEND_API_URL),
        )

        studyPlanText.change(
            fn=parseStudyPlanText,
            inputs=[studyPlanText],
            outputs=[studyPlanTextDataFrame],
            queue=True, 
            trigger_mode="once",
        )
        
        submitNewStudyPlan.submit(
            fn=None,
            inputs=submitNewStudyPlan,
            outputs=None, 
            queue=True,
            trigger_mode="once",
            js="""
            async (submitNewStudyPlan) => {
                function getCookie(name) {
                    const value = `; ${document.cookie}`;
                    const parts = value.split(`; ${name}=`);
                    if (parts.length === 2) return parts.pop().split(';').shift();
                    return null;
                }
                const token = getCookie('token');
                if (token) {
                    const btn = document.querySelector('.submit-button');
                    if (!btn) return;
                    
                    btn.disabled = true;
                    btn.style.opacity = '0.5';

                    // 隐藏登录相关元素
                    const startTime = document.querySelector('#startTime input').value;
                    const endTime = document.querySelector('#endTime input').value;

                    // 获取用户信息并填充表单
                    fetch(`BASE_URL/api/studyplans`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'fp': localStorage.getItem('visitorId')
                        },
                        body: JSON.stringify({
                            "startTime": startTime,
                            "endTime": endTime,
                            "eventContent": submitNewStudyPlan
                        }),
                        credentials: 'include'
                    })
                    .then(response => response.json())
                    .then(data => {
                        btn.disabled = false;
                        btn.style.opacity = '1';

                        if (data.code == 0) {
                            document.querySelector('#queryButton').click();
                        } else {
                            alert("submitNewStudyPlan error " + data.message);
                        }
                    });
                }
            }
            """.replace("BASE_URL", backend_api.BACKEND_API_URL),
        )

        suggestionText.change(
            fn=parseSuggestionText,
            inputs=[suggestionText],
            outputs=[suggestionExampleComponent.dataset],
            queue=True,
            trigger_mode="once",
        )
    return studyPage