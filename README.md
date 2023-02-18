集成步骤：

1. implementation 'com.github.tianxuefeng:TeneasyChatSdkDemo:1.1.8' （版本号请替换为稳定版或最新版）
2. gradle.properties: authToken=jp_3kctuti45o8ifvoi4nhmme0uk
3. settings.gradle:
  maven {
            url "https://jitpack.io"
            credentials { username authToken }
        }

4. AndroidManifest.xml

    添加一个Activity: android:name="com.teneasy.chatuisdk.ui.main.KeFuActivity"
       

5. 在页面添加一个按钮并触发事件：

    R.id.btn_contactSupport -> {
                val keFuIntent = Intent(this, KeFuActivity :: class.java)
                this.startActivity(keFuIntent)

完成。

小福，2023-02-18
