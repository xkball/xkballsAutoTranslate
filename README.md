# xkball's Auto Translation

![Discord](https://img.shields.io/discord/1370807259495534663?style=flat-square&logo=discord&label=xkball's%20mods&link=https%3A%2F%2Fdiscord.gg%2FS9DBXWHNsc)![GitHub License](https://img.shields.io/github/license/xkball/xkballsAutoTranslate?style=flat-square)

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

## Feature List 功能列表

| Minecraft Version | Mod Version | ConfigScreen | Translate Item Tooltip | Translate  FTBQ | Translate  Book |
| ----------------- | ----------- | ------------ | ---------------------- | --------------- | --------------- |
| 1.21.4/NeoForge   | 1.1.4       | ✅            | ✅                      |                 | ✅               |
| 1.21.1/NeoForge   | 1.0.4       | ✅            | ✅                      | ✅               | ✅               |
| 1.20.1/Forge      | 1.-1.3      |              | ✅                      | ✅               |                 |
| 1.19.2/Forge      | 1.-2.3      |              | ✅                      | ✅               |                 |


## Demonstrate  效果展示

![Translate Item Tooltip](https://github.com/xkball/xkballsAutoTranslate/blob/master/2025-02-28_19.24.23.png)

![Translate FTB Quest](https://github.com/xkball/xkballsAutoTranslate/blob/master/2025-02-28_19.25.29.png)

## Some Explanation 一些说明

- Cannot translate multi-line item tooltip, and have no solution for now.
- Can translate multi-line ftg quest, but not have a good support for multi-page quest. All translation will display in the last page.
- Special thanks: https://github.com/SihenZhang/AutoTranslator
- 无法连续翻译多行的物品 Tooltip，且暂时没有解决方案；
- FTB 任务可以一次翻译多行，但是对多页任务的支持暂不完善，会把所有翻译结果放在最后一页；
- 特别感谢: https://github.com/SihenZhang/AutoTranslator