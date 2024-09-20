# 银临茶舍


## 简介

银临茶舍APP，集资讯、听歌、美图、论坛、社交于一体的小银子聚集地
水群：828049503

<img height="192" src="./QQGroup.png" width="192" alt="QQGroup" />

## 数据

[![GitHub stars](https://img.shields.io/github/stars/masterQian/rachel.svg)](https://github.com/masterQian/rachel/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/masterQian/rachel.svg)](https://github.com/masterQian/rachel/network)

[![GitHub issues](https://img.shields.io/github/issues/masterQian/rachel.svg)](https://github.com/masterQian/rachel/issues)
[![GitHub license](https://img.shields.io/github/license/masterQian/rachel.svg)](https://github.com/masterQian/rachel/blob/master/LICENSE)

[![Star History Chart](https://api.star-history.com/svg?repos=masterQian/rachel&type=Date)](https://star-history.com/#masterQian/rachel&Date)

## 自定义歌曲打包教程

#### 下载自定义歌曲打包工具 (Windows版 / MacOS版)
RachelModMaker为可执行程序， music目录为待打包歌曲的所有配置文件

#### 打包
打开程序命令行选1
一个MOD可以包含一首或多首歌曲（确保你的所有歌曲的资源放置在music目录下）
其中每个资源文件的命名都以唯一的ID开头（非歌曲名，可在APP中查看信息，尽量避免与官方MOD重名）
，后面跟上分类信息（如果有的话）及文件后缀。

【配置信息】

配置信息以json文件保存

```json
{
    "version": "1.0",
    "author": "官方",
    "id": "棠梨煎雪",
    "name": "棠梨煎雪",
    "singer": "银临",
    "lyricist": "商连",
    "composer": "银临",
    "album": "腐草为萤",
    "bgd": false,
    "video": false,
    "chorus": [71670, 161140, 210400],
    "lyrics": {
        "line": [""],
        "pag": ["video"]
    }
}
```

| 字段名      | 类型   | 含义      | 是否必需 | 备注                      |
|----------|------|---------|------|-------------------------|
| version  | 字符串  | 歌曲版本    | √    |                         |
| author   | 字符串  | MOD作者   | √    |                         |
| id       | 字符串  | MOD唯一ID | √    | 与打包MOD的ID一致             |
| name     | 字符串  | 歌名      | √    |                         |
| singer   | 字符串  | 歌手      | √    |                         |
| lyricist | 字符串  | 作词      | √    |                         |
| composer | 字符串  | 作曲      | √    |                         |
| album    | 字符串  | 所属专辑    | √    |                         |
| bgd      | 布尔值  | 是否有动态壁纸 | √    |                         |
| video    | 布尔值  | 是否有PV视频 | √    |                         |
| chorus   | 整数数组 | 副歌点     | ×    | 单位为毫秒，如果没有这个字段歌曲就不能快进副歌 |
| lyrics   | 字典   | 歌词引擎    | √    | 字典中为歌词引擎对应一组歌词文件        |


【资源类型】

| 资源类型        | 分类信息         | 是否必需 | 备注             |
|-------------|--------------|------|----------------|
| 配置信息        | .json        | √    |
| 音源          | .flac        | √    |                |
| CD封面        | _record.webp | √    |                |
| 静态壁纸        | _bgs.webp    | √    |                |
| 静态歌词        | .lrc         | √    |                |
| 动态壁纸        | _bgd.webp    | ×    |                |
| PV视频        | .mp4         | ×    |                |
| 动态歌词(PAG引擎) | _xxx.pag     | ×    | xxx为不同的歌词动效文件名 |

例如，打包一首腐草为萤MOD，则至少要将【腐草为萤.json】，【腐草为萤.flac】，
【腐草为萤_record.webp】，【腐草为萤_bgs.webp】，【腐草为萤.lrc】这五个必需的
资源文件放置在music目录中，然后运行程序。
如果你有更多的资源可以额外补充， 如果要打包多首歌曲可以同时放在music目录中，最终会得到一个MOD文件

#### 解析

打开程序命令行选2，即可得到MOD中歌曲与配置信息

## 致谢

[![Picgo](https://www.picgo.net/content/images/system/logo_1650210921195_8a4898.svg)](https://www.picgo.net)

| 仓库                                                                                        | 作者                  |
|-------------------------------------------------------------------------------------------|---------------------|
| [UltimateBreadcrumbsView](https://github.com/AbdAlrahmanShammout/UltimateBreadcrumbsView) | AbdAlrahmanShammout |
| [glide](https://github.com/bumptech/glide)                                                | bumptech            |
| [BottomBarLayout](https://github.com/chaychan/BottomBarLayout)                            | chaychan            |
| [KLuban](https://github.com/forJrking/KLuban)                                             | forJrking           |
| [XUI](https://github.com/xuexiangjys/XUI)                                                 | xuexiangjys         |
| [okhttp](https://github.com/square/okhttp)                                                | square              |
| [PictureSelector](https://github.com/LuckSiege/PictureSelector)                           | LuckSiege           |
| [SmartRefreshLayout](https://github.com/scwang90/SmartRefreshLayout)                      | scwang90            |
| [html-textview](https://github.com/SufficientlySecure/html-textview)                      | SufficientlySecure  |
| [gson](https://github.com/google/gson)                                                    | google              |
| [material-components](https://github.com/material-components/material-components-android) | google              |
| [MMKV](https://github.com/Tencent/MMKV)                                                   | Tencent             |
| [libpag](https://github.com/Tencent/libpag)                                               | Tencent             |






