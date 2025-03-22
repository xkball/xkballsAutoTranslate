# xkball's Auto Translation

This mod add a key binding (default is T) that triggers machine  translation when the mouse is over an item or when the FTB quest details screen is open.

本模组添加了一个按键绑定（默认为 T），在鼠标指向一个物品或者打开 FTB 任务详情界面时按下按键绑定触发机器翻译。

Now this mod have three translators:

- Default:  Will not transltate anything but remind the user revise the config file.
- Google Translate: Use google translate web service.Don't need api key, and also cannot set an api key.
- Large language model: Use openAI api, user need set the api endpoint , api key and model to use.

目前有三个可用翻译器：

- 默认：不会进行任何翻译而是提醒你去修改配置文件；
- 谷歌翻译：使用谷歌翻译，注意根据网络环境，可能需要在配置文件中配置网络代理（全局网络代理可能不起作用）才能使用；
- 大语言模型翻译：按 OpenAI API 的方式调用大语言模型进行翻译。需要在配置文件中填写 API 提供者的 URL、KEY 和所使用的模型（另外暂时不支持 DeepSeek，因为其不支持 System Prompt）。

## Demonstrate  效果展示



## Some Explanation 一些说明

- Cannot translate multi-line item tooltip, and have no solution for now.
- Can translate multi-line ftg quest, but not have a good support for multi-page quest. All translation will display in the last page.
- Special thanks: https://github.com/SihenZhang/AutoTranslator
- 无法连续翻译多行的物品 Tooltip，且暂时没有解决方案；
- FTB 任务可以一次翻译多行，但是对多页任务的支持暂不完善，会把所有翻译结果放在最后一页；
- 特别感谢: https://github.com/SihenZhang/AutoTranslator