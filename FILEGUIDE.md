# 文件指南（File Guide）

本项目包含一个简单的贪吃蛇游戏，以下是各文件/脚本的说明：

## 📄 源代码文件

### `Snake.java`
- 程序入口类（`main` 方法）。
- 创建游戏窗口、加载 `GamePanel` 并启动游戏。
- 可以在此修改窗口标题、窗口大小、退出行为等。

### `GamePanel.java` （编译后存在 `GamePanel.class`）
- 游戏核心逻辑和渲染都在这个类中。
- 负责：
  - 初始化游戏状态（蛇、食物、方向等）
  - 定时刷新（`GAME_SPEED` 控制帧率）
  - 键盘输入（WASD/空格/R）
  - 更新蛇位置、碰撞判断、分数计算
  - 绘制游戏画面与 UI 文本

## 🧩 编译结果（类文件）

- `Snake.class`：`Snake.java` 编译后的字节码，Java 运行时执行该文件。
- `GamePanel.class`：`GamePanel.java` 编译后的字节码。
- `GamePanel$Direction.class` / `GamePanel$Node.class`：`GamePanel` 内部类的编译结果。

## 🛠️ 运行脚本

### `启动游戏.bat`
- 直接运行已编译的 `Snake.class`（假如已存在）。
- 适合在已经编译后无需重新编译时使用。

### `运行游戏.bat`
- 先执行 `javac -encoding UTF-8 Snake.java` 编译 Java 代码，然后运行游戏。
- 适合每次修改代码后用于快速测试。

## 📌 常见操作

- 修改游戏速度：在 `GamePanel` 中调整 `GAME_SPEED` 常量。
- 修改网格大小或游戏面积：修改 `GRID_SIZE`、`GRID_WIDTH`、`GRID_HEIGHT` 常量。
- 添加新功能（如障碍物、得分奖励等）：建议在 `GamePanel` 中扩展更新逻辑和渲染逻辑。

---

如果你希望项目支持更多平台（如生成 Windows 可执行文件），可以使用 Launch4j、jpackage 等工具打包。