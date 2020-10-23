# 如何开发一个新插件

## 插件工作过程

插件需要放置在`~/.chara/plugin/`目录下。在启动时，程序会自动读取并加载该目录下所有以`.plugin`结尾的文件，并且插件的加载总是优先于角色模型的加载。

插件可以同时存在多个，并且可以同时工作。

## 插件常见实现方式

当插件加载时，插件的`init(...)`和`launch()`函数会被调用，你可以在插件中实现任何代码逻辑，比方说在角色模型上添加一些额外元素，或者向`ChatbotManager`注册一个`Chatbot`实现，或者开启一个额外的窗口，甚至可以实现一个毫不相关的应用。

注意，在`init(...)`函数中，你可以放置资源读取代码，以及一些不可被回滚的逻辑，而在`launch()`函数中，你需要放置一些可回滚的逻辑。

与`launch()`函数对应的是`release()`函数，它用来释放插件依赖的资源，一般来说就是回滚`launch()`函数的内容。如果有其他动态添加的资源，也需要一并回滚。

已加载的插件会在右键菜单中展示，并且可以通过点击进行交互。当菜单中的插件对象被点击时，`clicked(...)`函数会被调用，一般来说可以用于“启用/禁用”插件，或者展示插件提供的额外窗口等。

插件接口本身非常简单，但是可以通过`EventBus`功能watch各类事件。

```java
var registration = EventBus.watch(event, v -> { });
```

watch函数会返回一个`registration`，你需要在插件中保存这些registration，并且在release函数中释放这些registration。

core模块公开提供的事件，可以在`Events`类中找到。

## .plugin文件结构

插件的扩展名为`.plugin`，其本身是一个zip压缩包。

其目录结构如下：

|               | 说明
|---------------|--------------------------------
| `plugin.json` | 插件配置文件
| `code`        | 存放插件相关的代码，目录下可以存放多个`.jar`文件

### plugin.json

插件配置文件，其内容如下：

```js
{
  "name": "插件的名字，例如dev",
  "version": 1000000, // 插件的版本号。格式是这样：比方说版本号是x.y.z，那么这里填 x * 1_000_000 + y * 1000 + z
  "compatibleMinCodeVersion": 1000000, // 基础代码的最低版本号
  "compatibleMaxCodeVersion": -1, // 基础代码的最高版本号，写-1表示没有要求
  "pluginClass": "插件实现的类名",
  "extra": { } // 该字段可选。这个字段的内容没有限制。可以存入各类自定义信息，比如作者、网站、git等信息。
}
```

### code

code目录中存放插件代码的`.jar`文件。需要将所有依赖的jar也一并放入其中，加载插件时会加载这里的所有`.jar`文件。

注意，这里的`.jar`文件需要包含`plugin.json`中配置的`pluginClass`，并且`module-info.java`中需要将对应的包export出来。

### 其他

在插件文件中还可以放置一些其他文件，例如LICENSE、README等。

### 参考目录

dev插件的目录结构的结构是这样的：

```
dev.plugin +
           |
           +-- plugin.json
           +-- code +
                    |
                    +-- dev.jar
```

## 开发流程

开发过程中因为需要经常调试和修改源码，不适合将代码打包入插件文件。  
这里给出一套开发流程供参考。描述比较简略，具体可以看`dev`插件工程的代码。此外推荐使用IDEA开发，可以使用免费的社区版。

### 1. clone

`git clone`本repo

### 2. 创建工程根目录

在`plugins/`目录下创建一个你的插件的子目录，作为工程根目录，例如`plugins/dev/`。

后文会多次出现“插件名字”，这里统一用变量符号`${name}`代替。

### 3. 使用gradle初始化

推荐使用`gradle wrapper`
可以直接从`dev`插件工程目录中，将这几个文件拷贝到你的目录里，然后做一些修改。

* `gradle`: gradle wrapper相关文件
* `gradlew`: linux/macos通过这个脚本使用gradlew
* `gradlew.bat`: windows通过这个脚本使用gradlew
* `build.gradle`: gradle配置文件1
* `settings.gradle`: gradle配置文件2

拷贝后，修改`settings.gradle`，将项目名称改为你的插件名称。修改`build.gradle`中的`archiveName`为合适的`jar`文件名。文件中其他参数视情
况自行增删。

### 4. 基础依赖

首先从release页面下载最新版的[vproxy.jar](https://github.com/wkgcass/vproxy)，放置在仓库根目录，命名为`vproxy.jar`。

在插件工程的`build.gradle`配置的`dependencies`中，需要确保有如下配置：

1. `compile files('../../core/build/libs/chara.jar')`，用于加载基础框架依赖。
2. `compile files('../../vproxy.jar')`，这是基础框架的依赖项，为了调试方便可以添加，打包时忽略即可
3. `compile group: 'com.1stleg', name: 'jnativehook', version: '2.1.0'`，这是基础框架依赖的一部分，为了调试方便可以添加，打包时忽略即可

在`build.gradle`配置的`javafx`中，需要确保有如下配置：

1. `modules = ['javafx.controls', 'javafx.swing']`，这也是基础框架的依赖，为了调试方便可以添加

### 5. 编译基础框架

进入`core`目录，运行`./gradlew clean jar`即可。

### 6. 开发

在你的新工程中创建一个类，命名为`${Name}Plugin`，并`implements Plugin`（实现Plugin接口）。你需要实现这个类。

在`launch()`函数中添加插件初始化时需要运行的代码。如果插件有额外资源分配的话，记得务必在`release()`中将资源释放。

在源代码根目录（一般来说是`src/main/java`）中，创建一个`module-info.java`，在其中`exports`你的插件所在的包。

### 7. 运行和调试

在你的新工程中创建一个包：`run.plugin.${name}`（例如`run.plugin.dev`）。  
创建一个类`Run`。写入如下内容：

```java
package run.plugin.${name};

import javafx.application.Application;
import javafx.stage.Stage;
import net.cassite.desktop.chara.Main;

public class Run extends Application {
    @Override
    public void start(Stage primaryStage) {
        new Main().start(primaryStage);
    }
}
```

然后在`module-info.java`中，把这个包也`exports`出来：`exports run.plugin.${name}`

通过`Run`类，即可对整个App进行调试。

### 8. 打包

在你的工程目录下，`./gradlew clean jar`，jar包会在`build/libs/`目录中生成。

### 9. 修改基础代码

有时候基础代码无法满足插件文件的需要，这时可以修改基础代码。
在修改完毕后，在基础代码的工程根目录（也就是`core/`目录）中，运行`./gradlew jar`即可。

注意，尽量不要`clean`基础代码，如果编译失败，jar文件消失，恢复后，可能需要重启IDEA才能重新找回依赖。