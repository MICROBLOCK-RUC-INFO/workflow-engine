/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define("vs/editor/editor.main.nls.zh-cn", {
    "vs/base/browser/ui/actionbar/actionbar": ["{0} ({1})"],
    "vs/base/browser/ui/aria/aria": ["{0} (已再次发生)"],
    "vs/base/browser/ui/findinput/findInput": ["输入"],
    "vs/base/browser/ui/findinput/findInputCheckboxes": ["区分大小写", "全字匹配", "使用正则表达式"],
    "vs/base/browser/ui/inputbox/inputBox": ["错误: {0}", "警告: {0}", "信息: {0}"],
    "vs/base/common/keybindingLabels": ["Ctrl", "Shift", "Alt", "Windows", "Control", "Shift", "Alt", "Command", "Control", "Shift", "Alt", "Windows"],
    "vs/base/common/severity": ["错误", "警告", "信息"],
    "vs/base/parts/quickopen/browser/quickOpenModel": ["{0}，选取器", "选取器"],
    "vs/base/parts/quickopen/browser/quickOpenWidget": ["快速选取器。键入以缩小结果范围。", "快速选取器"],
    "vs/base/parts/tree/browser/treeDefaults": ["折叠"],
    "vs/editor/browser/widget/diffEditorWidget": ["文件过大，无法比较。"],
    "vs/editor/browser/widget/diffReview": ["关闭", "第 {0} 个差异(共 {1} 个): 未修改 {2}, {3} 行，已修改 {4}, {5} 行", "空白", "未修改 {0}，已修改 {1}: {2}", "+ 已修改 {0}: {1}", "- 未修改 {0}: {1} ", "转至下一个差异", "转至上一个差异"],
    "vs/editor/common/config/commonEditorConfig": ["编辑器", "控制字体系列。", "控制字体粗细。", "以像素为单位控制字号。", "控制行高。使用 0 通过字号计算行高。", "以像素为单位控制字符间距。", "控制行号的显示。可能的值为“开”、“关”和“相对”。“相对”将显示从当前光标位置开始计数的行数。", "显示垂直标尺的列", "执行文字相关的导航或操作时将用作文字分隔符的字符", "一个制表符等于的空格数。该设置在 `editor.detectIndentation` 启用时根据文件内容进行重写。", "应为“number”。注意，值“auto”已由“editor.detectIndentation”设置替换。", '按 "Tab" 时插入空格。该设置在 `editor.detectIndentation` 启用时根据文件内容进行重写。', '应为 "boolean"。注意，值 "auto" 已由 "editor.detectIndentation" 设置替换。', '当打开文件时，将基于文件内容检测 "editor.tabSize" 和 "editor.insertSpaces"。', "控制选取范围是否有圆角", "控制编辑器是否可以滚动到最后一行之后", "控制是否显示 minimap", "控制是否自动隐藏迷你地图滑块。 ", "呈现某行上的实际字符(与颜色块相反)", "限制最小映射的宽度，尽量多地呈现特定数量的列", "控制是否将编辑器的选中内容作为搜索词填入到查找组件", "控制当编辑器中选中多个字符或多行文字时是否开启“在选定内容中查找”选项 ", "永不换行。", "将在视区宽度处换行。", '将在 "editor.wordWrapColumn" 处换行。', '将在最小视区和 "editor.wordWrapColumn" 处换行。', "控制折行方式。可以选择： - “off” （禁用折行）， - “on” （视区折行）， - “wordWrapColumn”（在“editor.wordWrapColumn”处折行）或 - “bounded”（在视区与“editor.wordWrapColumn”两者的较小者处折行）。", '在 "editor.wordWrap" 为 "wordWrapColumn" 或 "bounded" 时控制编辑器列的换行。', "控制折行的缩进。可以是“none”、“same”或“indent”。", '要对鼠标滚轮滚动事件的 "deltaX" 和 "deltaY" 使用的乘数 ', "映射到“Control”（Windows 和 Linux）或“Command”（OSX）。", "映射到“Alt”（Windows 和 Linux）或“Option”（OSX）。", "用鼠标添加多个光标时使用的修改键。“ctrlCmd”映射为“Control”（Windows 和 Linux）或“Command”（OSX）。“转到定义”和“打开链接”功能的鼠标手势将会相应调整，不与多光标修改键冲突。", "在字符串内启用快速建议。", "在注释内启用快速建议。", "在字符串和注释外启用快速建议。", "控制键入时是否应自动显示建议", "控制延迟多少毫秒后将显示快速建议", "启用在输入时显示含有参数文档和类型信息的小面板", "控制编辑器是否应该在左括号后自动插入右括号", "控制编辑器是否应在键入后自动设置行的格式", "控制编辑器是否应自动设置粘贴内容的格式。格式化程序必须可用并且能设置文档中某一范围的格式。", "控制编辑器是否应在用户键入、粘贴或移动行时自动调整缩进。语言的缩进规则必须可用。", "控制键入触发器字符时是否应自动显示建议", "控制按“Enter”键是否像按“Tab”键一样接受建议。这能帮助避免“插入新行”和“接受建议”之间的歧义。值为“smart”时表示，仅当文字改变时，按“Enter”键才能接受建议", '控制是否应在遇到提交字符时接受建议。例如，在 JavaScript 中，分号(";")可以为提交字符，可接受建议并键入该字符。', "在其他建议上方显示代码片段建议。", "在其他建议下方显示代码片段建议。", "在其他建议中穿插显示代码片段建议。", "不显示代码片段建议。", "控制是否将代码段与其他建议一起显示以及它们的排序方式。", "控制没有选择内容的复制是否复制当前行。", "控制是否应根据文档中的字数计算完成。", "建议小组件的字号", "建议小组件的行高", "控制编辑器是否应突出显示选项的近似匹配", "控制编辑器是否应该突出显示语义符号次数", "控制可在概述标尺同一位置显示的效果数量", "控制概述标尺周围是否要绘制边框。", '控制光标动画样式，可能的值为 "blink"、"smooth"、"phase"、"expand" 和 "solid"', "通过使用鼠标滚轮同时按住 Ctrl 可缩放编辑器的字体", '控制光标样式，接受的值为 "block"、"block-outline"、"line"、"line-thin" 、"underline" 和 "underline-thin"', "启用字体连字", "控制光标是否应隐藏在概述标尺中。", "控制编辑器中呈现空白字符的方式，可能为“无”、“边界”和“全部”。“边界”选项不会在单词之间呈现单空格。", "控制编辑器是否应呈现控制字符", "控制编辑器是否应呈现缩进参考线", "控制编辑器应如何呈现当前行突出显示，可能为“无”、“装订线”、“线”和“全部”。", "控制编辑器是否显示代码滤镜", "控制编辑器是否启用代码折叠功能", "控制是否自动隐藏导航线上的折叠控件。", "当选择其中一项时，将突出显示匹配的括号。", "控制编辑器是否应呈现垂直字形边距。字形边距最常用于调试。", "在制表位后插入和删除空格", "删除尾随自动插入的空格", "即使在双击编辑器内容或按 Esc 键时，也要保持速览编辑器的打开状态。", "控制编辑器是否应该允许通过拖放移动所选项。", "编辑器将使用平台 API 以检测是否附加了屏幕阅读器。", "编辑器将对屏幕阅读器的使用进行永久优化。", "编辑器将不再对屏幕阅读器的使用进行优化。", "控制编辑器是否应运行在对屏幕阅读器进行优化的模式。", "控制编辑器是否应检测链接并使它们可被点击", "控制 Diff 编辑器以并排或内联形式显示差异", "控制差异编辑器是否将对前导空格或尾随空格的更改显示为差异", "控制差异编辑器是否为已添加/删除的更改显示 +/- 指示符号", "控制是否支持 Linux 主剪贴板。"],
    "vs/editor/common/config/editorOptions": ["现在无法访问编辑器。按 Alt+F1  显示选项。", "编辑器内容"],
    "vs/editor/common/controller/cursor": ["执行命令时出现意外异常。"],
    "vs/editor/common/model/textModelWithTokens": ["标记输入时模式失败。"],
    "vs/editor/common/modes/modesRegistry": ["纯文本"],
    "vs/editor/common/services/bulkEdit": ["这些文件也已同时更改: {0}", "未做编辑", "在 {1} 个文件中进行了 {0} 次编辑", "在 1 个文件中进行了 {0} 次编辑"],
    "vs/editor/common/services/modelServiceImpl": ["[{0}]\n{1}", "[{0}] {1}"],
    "vs/editor/common/view/editorColorRegistry": ["光标所在行高亮内容的背景颜色。", "光标所在行四周边框的背景颜色。", '突出显示范围的背景颜色，例如 "Quick Open" 和“查找”功能。', "编辑器光标颜色。", "编辑器光标的背景色。可以自定义块型光标覆盖字符的颜色。", "编辑器中空白字符颜色。", "编辑器缩进参考线颜色。", "编辑器行号颜色。", "编辑器标尺的颜色。", "编辑器 CodeLens 的前景色", "匹配括号的背景色", "匹配括号外框颜色", "概览标尺边框的颜色。", "编辑器导航线的背景色。导航线包括边缘符号和行号。", "编辑器中错误波浪线的前景色。", "编辑器中错误波浪线的边框颜色。", "编辑器中警告波浪线的前景色。", "编辑器中警告波浪线的边框颜色。"],
    "vs/editor/contrib/bracketMatching/common/bracketMatching": ["转到括号"],
    "vs/editor/contrib/caretOperations/common/caretOperations": ["将插入点左移", "将插入点右移"],
    "vs/editor/contrib/caretOperations/common/transpose": ["转置字母"],
    "vs/editor/contrib/clipboard/browser/clipboard": ["剪切", "复制", "粘贴", "复制并突出显示语法"],
    "vs/editor/contrib/comment/common/comment": ["切换行注释", "添加行注释", "删除行注释", "切换块注释"],
    "vs/editor/contrib/contextmenu/browser/contextmenu": ["显示编辑器上下文菜单"],
    "vs/editor/contrib/find/browser/findWidget": ["查找", "查找", "上一个匹配", "下一个匹配", "在选定内容中查找", "关闭", "替换", "替换", "替换", "全部替换", "切换替换模式", "仅前 999 个结果突出显示，但所有查找操作均针对整个文本。", "第 {0} 个(共 {1} 个)", "无结果"],
    "vs/editor/contrib/find/common/findController": ["查找", "查找下一个", "查找上一个", "查找下一个选择", "查找上一个选择", "替换", "将选择添加到下一个查找匹配项", "将选择内容添加到上一查找匹配项", "将上次选择移动到下一个查找匹配项", "将上个选择内容移动到上一查找匹配项", "选择所有找到的查找匹配项", "更改所有匹配项", "显示下一个搜索结果", "显示上一个搜索结果"],
    "vs/editor/contrib/folding/browser/folding": ["展开", "以递归方式展开", "折叠", "以递归方式折叠", "全部折叠", "全部展开", "折叠级别 {0}"],
    "vs/editor/contrib/format/browser/formatActions": ["在第 {0} 行进行了 1 次格式编辑", "在第 {1} 行进行了 {0} 次格式编辑", "第 {0} 行到第 {1} 行间进行了 1 次格式编辑", "第 {1} 行到第 {2} 行间进行了 {0} 次格式编辑", "格式化文件", "格式化选定代码"],
    "vs/editor/contrib/goToDeclaration/browser/goToDeclarationCommands": ["未找到“{0}”的任何定义", "找不到定义", " – {0} 定义", "转到定义", "打开侧边的定义", "查看定义", "未找到“{0}”的实现", "未找到实现", "– {0} 个实现", "转到实现", "速览实现", "未找到“{0}”的类型定义", "未找到类型定义", " – {0} 个类型定义", "转到类型定义", "快速查看类型定义"],
    "vs/editor/contrib/goToDeclaration/browser/goToDeclarationMouse": ["单击显示 {0} 个定义。"],
    "vs/editor/contrib/gotoError/browser/gotoError": ["({0}/{1})", "转到下一个错误或警告", "转到上一个错误或警告", "编辑器标记导航小组件错误颜色。", "编辑器标记导航小组件警告颜色。", "编辑器标记导航小组件背景色。"],
    "vs/editor/contrib/hover/browser/hover": ["显示悬停"],
    "vs/editor/contrib/hover/browser/modesContentHover": ["正在加载..."],
    "vs/editor/contrib/inPlaceReplace/common/inPlaceReplace": ["替换为上一个值", "替换为下一个值"],
    "vs/editor/contrib/linesOperations/common/linesOperations": ["向上复制行", "向下复制行", "向上移动行", "向下移动行", "按升序排列行", "按降序排列行", "裁剪尾随空格", "删除行", "行缩进", "行减少缩进", "在上面插入行", "在下面插入行", "删除左侧所有内容", "删除右侧所有内容", "合并行", "转置游标处的字符", "转换为大写", "转换为小写"],
    "vs/editor/contrib/links/browser/links": ["Cmd + 单击以跟踪链接", "Ctrl + 单击以跟踪链接", "Cmd + 单击以执行命令", "Ctrl + 单击以执行命令", "Alt + 单击以访问链接", "Alt + 单击以执行命令", "抱歉，无法打开此链接，因为其格式不正确: {0}", "抱歉，无法打开此链接，因为其目标丢失。", "打开链接"],
    "vs/editor/contrib/multicursor/common/multicursor": ["在上面添加光标", "在下面添加光标", "在行尾添加光标"],
    "vs/editor/contrib/parameterHints/browser/parameterHints": ["触发参数提示"],
    "vs/editor/contrib/parameterHints/browser/parameterHintsWidget": ["{0}，提示"],
    "vs/editor/contrib/quickFix/browser/quickFixCommands": ["显示修补程序({0})", "显示修补程序", "快速修复"],
    "vs/editor/contrib/referenceSearch/browser/referenceSearch": [" – {0} 个引用", "查找所有引用"],
    "vs/editor/contrib/referenceSearch/browser/referencesController": ["正在加载..."],
    "vs/editor/contrib/referenceSearch/browser/referencesModel": ["在文件 {0} 的 {1} 行 {2} 列的符号", "{0}  中有 1 个符号，完整路径：{1}", "{1} 中有 {0} 个符号，完整路径：{2}", "未找到结果", "在 {0} 中找到 1 个符号", "在 {1} 中找到 {0} 个符号", "在 {1} 个文件中找到 {0} 个符号"],
    "vs/editor/contrib/referenceSearch/browser/referencesWidget": ["解析文件失败。", "{0} 个引用", "{0} 个引用", "无可用预览", "引用", "无结果", "引用", "速览视图标题区域背景颜色。", "速览视图标题颜色。", "速览视图标题信息颜色。", "速览视图边框和箭头颜色。", "速览视图结果列表背景颜色。", "速览视图结果列表中行节点的前景色。", "速览视图结果列表中文件节点的前景色。", "速览视图结果列表中所选条目的背景颜色。", "速览视图结果列表中所选条目的前景色。", "速览视图编辑器背景颜色。", "速览视图编辑器中导航线的背景颜色。", "在速览视图结果列表中匹配突出显示颜色。", "在速览视图编辑器中匹配突出显示颜色。"],
    "vs/editor/contrib/rename/browser/rename": ["无结果。", "成功将“{0}”重命名为“{1}”。摘要：{2}", "抱歉，重命名无法执行。", "重命名符号"],
    "vs/editor/contrib/rename/browser/renameInputField": ['重命名输入。键入新名称并按 "Enter" 提交。'],
    "vs/editor/contrib/smartSelect/common/smartSelect": ["扩大选择", "缩小选择"],
    "vs/editor/contrib/suggest/browser/suggestController": ["确认“{0}”插入以下文本：{1}", "触发建议"],
    "vs/editor/contrib/suggest/browser/suggestWidget": ["建议小组件的背景颜色", "建议小组件的边框颜色", "建议小组件的前景颜色。", "建议小组件中被选择条目的背景颜色。", "建议小组件中匹配内容的高亮颜色。", "阅读详细信息...{0}", "{0}(建议)具有详细信息", "{0}，建议", "阅读简略信息...{0}", "正在加载...", "无建议。", "{0}，已接受", "{0}(建议)具有详细信息", "{0}，建议"],
    "vs/editor/contrib/toggleTabFocusMode/common/toggleTabFocusMode": ["切换 Tab 键是否移动焦点"],
    "vs/editor/contrib/wordHighlighter/common/wordHighlighter": ["读取访问时符号的背景颜色，例如读取变量时。", "写入访问时符号的背景颜色，例如写入变量时。"],
    "vs/editor/contrib/zoneWidget/browser/peekViewWidget": ["关闭"],
    "vs/editor/standalone/browser/inspectTokens/inspectTokens": ["Developer: Inspect Tokens"],
    "vs/editor/standalone/browser/quickOpen/gotoLine": ["Go to line {0} and character {1}", "Go to line {0}", "Type a line number between 1 and {0} to navigate to", "Type a character between 1 and {0} to navigate to", "Go to line {0}", "Type a line number, followed by an optional colon and a character number to navigate to", "Go to Line..."],
    "vs/editor/standalone/browser/quickOpen/quickCommand": ["{0}, commands", "Type the name of an action you want to execute", "Command Palette"],
    "vs/editor/standalone/browser/quickOpen/quickOutline": ["{0}, symbols", "Type the name of an identifier you wish to navigate to", "Go to Symbol...", "symbols ({0})", "modules ({0})", "classes ({0})", "interfaces ({0})", "methods ({0})", "functions ({0})", "properties ({0})", "variables ({0})", "variables ({0})", "constructors ({0})", "calls ({0})"],
    "vs/editor/standalone/browser/standaloneCodeEditor": ["Editor content", "Press Ctrl+F1 for Accessibility Options.", "Press Alt+F1 for Accessibility Options."],
    "vs/editor/standalone/browser/toggleHighContrast/toggleHighContrast": ["Toggle High Contrast Theme"],
    "vs/platform/configuration/common/configurationRegistry": ["默认配置替代", "针对 {0} 语言，配置替代编辑器设置。", "针对某种语言，配置替代编辑器设置。", '无法注册“{0}”。其符合描述特定语言编辑器设置的表达式 "\\\\[.*\\\\]$"。请使用 "configurationDefaults"。', "无法注册“{0}”。此属性已注册。"],
    "vs/platform/keybinding/common/abstractKeybindingService": ["已按下({0})。正在等待同时按下第二个键...", "组合键({0}, {1})不是命令。"],
    "vs/platform/message/common/message": ["关闭", "稍后", "取消"],
    "vs/platform/theme/common/colorRegistry": ["颜色格式无效。请使用 #RGB、#RGBA、#RRGGBB 或 #RRGGBBAA", "工作台中使用的颜色。", "整体前景色。此颜色仅在不被组件覆盖时适用。", "错误信息的整体前景色。此颜色仅在不被组件覆盖时适用。", "提供其他信息的说明文本的前景色，例如标签文本。", "焦点元素的整体边框颜色。此颜色仅在不被其他组件覆盖时适用。", "在元素周围额外的一层边框，用来提高对比度从而区别其他元素。", "在活动元素周围额外的一层边框，用来提高对比度从而区别其他元素。", "工作台所选文本的背景颜色（例如输入字段或文本区域）。注意，本设置不适用于编辑器。", "文字分隔符的颜色。", "文本中链接的前景色。", "文本中活动链接的前景色。", "预格式化文本段的前景色。", "文本中块引用的背景颜色。", "文本中块引用的边框颜色。", "文本中代码块的背景颜色。", "编辑器内小组件（如查找/替换）的阴影颜色。", "输入框背景色。", "输入框前景色。", "输入框边框。", "输入字段中已激活选项的边框颜色。", "输入框中占位符的前景色。", "严重性为信息时输入验证的背景颜色。", "严重性为信息时输入验证的边框颜色。", "严重性为警告时输入验证的背景颜色。", "严重性为警告时输入验证的边框颜色。", "严重性为错误时输入验证的背景颜色。", "严重性为错误时输入验证的边框颜色。", "下拉列表背景色。", "下拉列表前景色。", "下拉列表边框。", "焦点项在列表或树活动时的背景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "焦点项在列表或树活动时的背景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "已选项在列表或树活动时的背景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "已选项在列表或树活动时的前景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "已选项在列表或树非活动时的背景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "已选项在列表或树非活动时的前景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "已选项在列表或树非活动时的背景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "已选项在列表或树非活动时的前景颜色。活动的列表或树具有键盘焦点，非活动的没有。", "使用鼠标移动项目时，列表或树的背景颜色。", "鼠标在项目上悬停时，列表或树的前景颜色。", "使用鼠标移动项目时，列表或树进行拖放的背景颜色。", "在列表或树中搜索时，其中匹配内容的高亮颜色。", "快速选取器分组标签的颜色。", "快速选取器分组边框的颜色。", "按钮前景色。", "按钮背景色。", "按钮在悬停时的背景颜色。", "Badge 背景色。Badge 是小型的信息标签，如表示搜索结果数量的标签。", "Badge 前景色。Badge 是小型的信息标签，如表示搜索结果数量的标签。", "表示视图被滚动的滚动条阴影。", "滚动条滑块背景色", "滚动条滑块在悬停时的背景色", "滚动条滑块被激活时的背景色", "表示长时间操作的进度条的背景色。", "编辑器背景颜色。", "编辑器默认前景色。", "编辑器组件(如查找/替换)背景颜色。", "编辑器小部件的边框颜色。此颜色仅在小部件有边框且不被小部件重写时适用。", "编辑器所选内容的颜色。", "用以彰显高对比度的所选文本的颜色。", "非活动编辑器中所选内容的颜色。", "与所选内容具有相同内容的区域颜色。", "当前搜索匹配项的颜色。", "其他搜索匹配项的颜色。", "限制搜索的范围的颜色。", "悬停提示显示时文本底下的高亮颜色。", "编辑器悬停提示的背景颜色。", "光标悬停时编辑器的边框颜色。", "活动链接颜色。", "已插入文本的背景颜色。", "被删除文本的背景颜色。", "插入的文本的轮廓颜色。", "被删除文本的轮廓颜色。", "内联合并冲突中当前版本区域的标头背景色。", "内联合并冲突中当前版本区域的内容背景色。", "内联合并冲突中传入的版本区域的标头背景色。", "内联合并冲突中传入的版本区域的内容背景色。", "内联合并冲突中共同祖先区域的标头背景色。", "内联合并冲突中共同祖先区域的内容背景色。", "内联合并冲突中标头和分割线的边框颜色。", "内联合并冲突中当前版本区域的概览标尺前景色。", "内联合并冲突中传入的版本区域的概览标尺前景色。", "内联合并冲突中共同祖先区域的概览标尺前景色。"]
});
//# sourceMappingURL=../../../min-maps/vs/editor/editor.main.nls.zh-cn.js.map
