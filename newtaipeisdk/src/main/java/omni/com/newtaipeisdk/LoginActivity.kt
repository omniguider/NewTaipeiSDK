package omni.com.newtaipeisdk

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import omni.com.newtaipeisdk.NewTaipeiSDKActivity.LOG_TAG
import omni.com.newtaipeisdk.manager.UserInfoManager
import omni.com.newtaipeisdk.model.CommonResponse
import omni.com.newtaipeisdk.model.ConfigResponse
import omni.com.newtaipeisdk.model.user.LoginData
import omni.com.newtaipeisdk.network.NetworkManager
import omni.com.newtaipeisdk.network.NetworkManager.NetworkManagerListener
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi
import omni.com.newtaipeisdk.tool.DialogTools
import omni.com.newtaipeisdk.tool.PreferencesTools
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private val ARG_KEY_USERNAME = "arg_key_username"
    private val ARG_KEY_USERID = "arg_key_userid"

    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: PromptInfo? = null
    private var biometric: String? = "false"
    private var accountEdt: EditText? = null
    private var passwordEdt: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkLocationService()
        checkBluetoothOn()

        NewTaipeiSDKApi.getInstance().getConfig(
            this, object : NetworkManagerListener<ConfigResponse?> {
                override fun onSucceed(response: ConfigResponse?) {
                    if (response!!.enabled == "false") {
                        val dialog = Dialog(this@LoginActivity)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.dialog_confirm)
                        dialog.setCancelable(false)
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        val width =
                            (resources.displayMetrics.widthPixels * 0.9).toInt()
                        dialog.window!!.setLayout(
                            width,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        val title: TextView = dialog.findViewById(R.id.title)
                        title.text = "提示"
                        val content: TextView = dialog.findViewById(R.id.content)
                        content.text = response.disabled_message
                        val btn: TextView = dialog.findViewById(R.id.btn)
                        btn.text = "確認"
                        btn.setOnClickListener {
                            dialog.dismiss()
                            finish()
                        }
                        dialog.show()
                    } else {
                        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            packageManager.getPackageInfo(packageName, 0).longVersionCode
                        } else {
                            @Suppress("DEPRECATION")
                            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
                        }
                        if (response.android_version.toLong() > versionCode) {
                            val dialog = Dialog(this@LoginActivity)
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.setContentView(R.layout.dialog_confirm)
                            dialog.setCancelable(false)
                            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            val width =
                                (resources.displayMetrics.widthPixels * 0.9).toInt()
                            dialog.window!!.setLayout(
                                width,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            val title: TextView = dialog.findViewById(R.id.title)
                            title.text = "版本更新提醒"
                            val content: TextView = dialog.findViewById(R.id.content)
                            content.text = "發現新版本！為了獲得最佳使用體驗，請前往下載最新版本的應用程式。"
                            val btn: TextView = dialog.findViewById(R.id.btn)
                            btn.text = "前往下載"
                            btn.setOnClickListener {
                                dialog.dismiss()
                                val url = response.android_app_url
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(url)
                                }
                                startActivity(intent)
                                finish()
                            }
                            dialog.show()
                        }
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                }
            })

        val keepPassword = if (PreferencesTools.getInstance().getProperty(
                this,
                PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD
            ) != null
        ) {
            PreferencesTools.getInstance().getProperty(
                this,
                PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD
            )
        } else {
            "false"
        }
        if (keepPassword == "true" && hasLoginToken()) {
            verifyLoginToken()
        } else if (keepPassword == "true") {
            PreferencesTools.getInstance().saveProperty(
                this,
                PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD,
                "false"
            )
        }

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this@LoginActivity,
            executor!!, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d(LOG_TAG, "Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(LOG_TAG, "Authentication succeeded!")

                    if (hasLoginToken()) {
                        verifyLoginToken()
                    } else {
                        DialogTools.getInstance().showErrorMessage(
                            this@LoginActivity, R.string.dialog_title_text_note,
                            R.string.dialog_message_login_by_account
                        )
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d(LOG_TAG, "Authentication failed")
                }
            })

        promptInfo = PromptInfo.Builder()
            .setTitle("登入行動差勤按刷卡")
            .setSubtitle("")
            .setNegativeButtonText("使用帳號/密碼")
            .setConfirmationRequired(false)
            .build()

        accountEdt = findViewById(R.id.account_et)
        passwordEdt = findViewById(R.id.password_et)
        if ((UserInfoManager.getInstance()
                .getUserInfo(this@LoginActivity) != null)
        ) {
            accountEdt!!.setText(
                UserInfoManager.getInstance()
                    .getUserInfo(this@LoginActivity)!!
                    .account
            )
        }

        findViewById<LinearLayout>(R.id.fingerprint_layout).setOnClickListener { view: View? ->
            biometricPrompt!!.authenticate(
                promptInfo!!
            )
        }

        findViewById<TextView>(R.id.fragment_login_tv_login).setOnClickListener { view: View? ->
            if (accountEdt?.text!!.isEmpty()) {
                showDialog("請輸入帳號")
            } else if (passwordEdt?.text!!.isEmpty()) {
                showDialog("請輸入密碼")
            } else {
                NewTaipeiSDKApi.getInstance().verifyLoginDevice(
                    this@LoginActivity,
                    accountEdt!!.text.trim().toString(),
                    object : NetworkManager.NetworkManagerListener<CommonResponse> {
                        override fun onSucceed(response: CommonResponse) {
                            if (response.result == "true") {
                                login()
                            } else {
                                val dialog = Dialog(this@LoginActivity)
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                dialog.setContentView(R.layout.dialog_yes_no)
                                dialog.setCancelable(false)
                                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                val width =
                                    (resources.displayMetrics.widthPixels * 0.9).toInt()
                                dialog.window!!.setLayout(
                                    width,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                val content: TextView = dialog.findViewById(R.id.content)
                                content.text =
                                    "您已於其他設備登入打卡系統，若選擇繼續將強制登出前次設備。"
                                val noBtn: TextView = dialog.findViewById(R.id.no)
                                noBtn.text = "離開"
                                noBtn.setOnClickListener {
                                    dialog.dismiss()
                                }
                                val yesBtn: TextView = dialog.findViewById(R.id.yes)
                                yesBtn.text = "繼續"
                                yesBtn.setOnClickListener {
                                    login()
                                }
                                dialog.show()
                            }
                        }

                        override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                            showDialog(errorMsg)
                        }
                    })
            }
        }
    }

    override fun onResume() {
        super.onResume()

        biometric = if (PreferencesTools.getInstance().getProperty(
                this@LoginActivity,
                PreferencesTools.KEY_PREFERENCES_BIOMETRIC
            ) != null
        ) {
            PreferencesTools.getInstance().getProperty(
                this@LoginActivity,
                PreferencesTools.KEY_PREFERENCES_BIOMETRIC
            )
        } else {
            "false"
        }

        if (biometric == "true")
            findViewById<LinearLayout>(R.id.fingerprint_layout).visibility = View.VISIBLE
        else
            findViewById<LinearLayout>(R.id.fingerprint_layout).visibility = View.INVISIBLE
    }

    private fun verifyLoginToken() {
        val userInfo = UserInfoManager.getInstance().getUserInfo(this@LoginActivity)
        if (userInfo?.account.isNullOrBlank() || userInfo?.token.isNullOrBlank()) {
            PreferencesTools.getInstance().saveProperty(
                this@LoginActivity,
                PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD,
                "false"
            )
            PreferencesTools.getInstance().saveProperty(
                this@LoginActivity,
                PreferencesTools.KEY_PREFERENCES_BIOMETRIC,
                "false"
            )
            return
        }
        val account = userInfo?.account.orEmpty()
        val token = userInfo?.token.orEmpty()
        val name = userInfo?.name.orEmpty()

        NewTaipeiSDKApi.getInstance().verifyLoginToken(
            this@LoginActivity,
            account,
            token, "Y",
            object : NetworkManager.NetworkManagerListener<CommonResponse> {
                override fun onSucceed(response: CommonResponse) {
                    if (response.result == "true") {
                        val intent =
                            Intent(this@LoginActivity, NewTaipeiSDKActivity::class.java)
                        intent.putExtra(
                            ARG_KEY_USERNAME,
                            name
                        )
                        intent.putExtra(
                            ARG_KEY_USERID,
                            account
                        )
                        startActivity(intent)
                        finish()
                    } else {
                        val dialog = Dialog(this@LoginActivity)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setContentView(R.layout.dialog_confirm)
                        dialog.setCancelable(false)
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
                        dialog.window!!.setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT)

                        val title = dialog.findViewById<TextView>(R.id.title)
                        title.text = "提示"

                        var error = response.errorMessage
                        if (error.contains("ACCESS_DENY(t)")) {
                            error = "登入資訊已失效，請重新登入"
                        } else if (error.contains("ACCESS_DENY")) {
                            error = "安全驗證失敗"
                        } else if (error.contains("INVALID_PARAMETER")) {
                            error = "傳入參數錯誤"
                        } else if (error.contains("INVALID_LDAP")) {
                            error = "伺服器連接失敗"
                        }
                        val content = dialog.findViewById<TextView>(R.id.content)
                        content.text = error

                        val btn = dialog.findViewById<TextView>(R.id.btn)
                        btn.text = "確認"
                        btn.setOnClickListener { v: View? ->
                            dialog.dismiss()
                            PreferencesTools.getInstance().saveProperty(
                                this@LoginActivity,
                                PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD, "false"
                            )
                            PreferencesTools.getInstance().saveProperty(
                                this@LoginActivity,
                                PreferencesTools.KEY_PREFERENCES_BIOMETRIC, "false"
                            )
                            UserInfoManager.getInstance()
                                .userLogout(this@LoginActivity)

                            val intent =
                                Intent(
                                    this@LoginActivity,
                                    LoginActivity::class.java
                                )
                            startActivity(intent)
                            finish()
                        }
                        dialog.show()
                    }
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                    showDialog(errorMsg)
                }
            })
    }

    private fun hasLoginToken(): Boolean {
        val userInfo = UserInfoManager.getInstance().getUserInfo(this@LoginActivity)
        return !userInfo?.account.isNullOrBlank() && !userInfo?.token.isNullOrBlank()
    }

    private fun login() {
        NewTaipeiSDKApi.getInstance().login(
            this@LoginActivity,
            accountEdt!!.text.trim().toString(),
            passwordEdt!!.text.trim().toString(),
            object : NetworkManager.NetworkManagerListener<Array<LoginData?>> {
                override fun onSucceed(userLoginData: Array<LoginData?>) {
                    PreferencesTools.getInstance().saveProperty(
                        this@LoginActivity,
                        PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD, "true"
                    )

                    UserInfoManager.getInstance().saveUserLoginInfo(
                        this@LoginActivity, userLoginData[0],
                        accountEdt!!.text.trim().toString(),
                        passwordEdt!!.text.trim().toString()
                    )
                    val intent =
                        Intent(this@LoginActivity, NewTaipeiSDKActivity::class.java)
                    intent.putExtra(ARG_KEY_USERNAME, userLoginData[0]!!.name)
                    intent.putExtra(ARG_KEY_USERID, userLoginData[0]!!.idcount)
                    startActivity(intent)
                    finish()
                }

                override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                    var error = errorMsg
                    if (errorMsg.contains("ACCESS_DENY"))
                        error = "安全驗證失敗"
                    else if (errorMsg.contains("INVALID_PARAMETER"))
                        error = "傳入參數錯誤"
                    else if (errorMsg.contains("INVALID_LDAP"))
                        error = "伺服器連接失敗"
                    else if (errorMsg.contains("LOGIN_FAILED"))
                        error = "使用者帳號或密碼錯誤"
                    showDialog(error)
                }
            })
    }

    private fun showDialog(text: String?) {
        val dialog = Dialog(this@LoginActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirm)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width =
            (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window!!.setLayout(
            width,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val title: TextView = dialog.findViewById(R.id.title)
        title.text = "提示"
        val content: TextView = dialog.findViewById(R.id.content)
        content.text = text
        val btn: TextView = dialog.findViewById(R.id.btn)
        btn.text = "確認"
        btn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkLocationService() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ensurePermissions()
        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("位置服務尚未開啟，請設定")
            dialog.setPositiveButton(
                "open settings"
            ) { paramDialogInterface, paramInt -> // TODO Auto-generated method stub
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            dialog.setNegativeButton(
                "cancel"
            ) { paramDialogInterface, paramInt -> // TODO Auto-generated method stub
                val dialog = AlertDialog.Builder(this@LoginActivity)
                dialog.setMessage("沒有開啟位置服務，無法掃描藍芽設備")
                dialog.show()
            }
            dialog.show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothOn() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
        } else {
            if (!bluetoothAdapter.isEnabled) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, 77)
            }
        }
    }


    private fun ensurePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if ((ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED || (
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_ADMIN
                        ) != PackageManager.PERMISSION_GRANTED) || (
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED) || (
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ), NewTaipeiSDKActivity.REQUEST_PERMISSIONS
                )
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED || (
                        ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_ADMIN
                        ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), NewTaipeiSDKActivity.REQUEST_PERMISSIONS
                )
            }
        }
    }
}
