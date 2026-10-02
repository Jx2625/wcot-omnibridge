## 构建要求

本项目依赖以下模组的 jar（`build.gradle` 通过 `flatDir` 从 `libs/` 加载），
请自行下载并放入 `libs/` 目录：

- WCWT 1.3.10
- UselessMod 1.21.1-2.4.5.3
- AE2WTLib 19.5.1（如果 Maven 解析失败）
- AE2 19.2.18（如果 Maven 解析失败）

## 功能 / Features

- ✅ 在 WCWT 的样板编码终端里，通过 JEI 的 “+” 按钮直接传输合金炉配方
- ✅ 复用 UselessMod 的完整转换逻辑，支持模具、标签、动态输入等全部特性

- ✅ Transfer Alloy Furnace recipes from JEI into WCWT's pattern encoding terminal via the "+" button
- ✅ Reuses UselessMod's full conversion pipeline: molds, tags, dynamic inputs, and more

## 使用 / Usage

1. 打开 WCWT 的无线综合工作终端
2. 在 JEI 里找到任意一条万象合金炉配方
3. 点击 “+” 按钮把配方传输到编码终端
4. 点“编码”按钮，输出槽里就会得到万象样板
***
1. Open the WCWT Wireless Comprehensive Work Terminal
2. Find any Alloy Furnace recipe in JEI
3. Click the "+" button to transfer the recipe into the encoding terminal
4. Press "Encode" — the output slot will contain an Omniversal Pattern

## 致谢 / Credits

本项目基于以下模组的公开 API 进行桥接，它们的代码均在 MIT 协议下发布：

This project bridges the public APIs of the following mods, all licensed under MIT:

- AE2 WCWT by lhy
  https://github.com/lhy512103/AE2-WCWT

- UselessMod by SorrowMist
  https://github.com/SorrowMist/UselessMod