集成步骤：

1. implementation 'com.github.tianxuefeng:TeneasyChatSdkDemo:1.1.8' （版本号请替换为稳定版或最新版）

2. AndroidManifest.xml


 <activity android:name="com.teneasy.chatuisdk.ui.main.KeFuActivity"  android:exported="true">
            <intent-filter>
                <category android:name="android.intent.category.DEFAULT" />

                <action android:name="android.intent.action.VIEW" />
            </intent-filter>
        </activity>

3. 在页面添加一个按钮并触发事件：


    R.id.btn_contactSupport -> {
                val keFuIntent = Intent(this, KeFuActivity :: class.java)
                this.startActivity(keFuIntent)

完成。

小福，2023-02-18
