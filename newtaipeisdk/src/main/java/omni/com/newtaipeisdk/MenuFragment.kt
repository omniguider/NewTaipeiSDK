package omni.com.newtaipeisdk

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import omni.com.newtaipeisdk.NewTaipeiSDKActivity.userid
import omni.com.newtaipeisdk.manager.UserInfoManager
import omni.com.newtaipeisdk.model.LogoutResponse
import omni.com.newtaipeisdk.model.user.LoginData
import omni.com.newtaipeisdk.network.NetworkManager
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi
import omni.com.newtaipeisdk.tool.DialogTools
import omni.com.newtaipeisdk.tool.PreferencesTools

class MenuFragment : Fragment() {
    private var mView: View? = null
    private var switchPassword: SwitchCompat? = null
    private var switchFingerprint: SwitchCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_menu, container, false)

            mView?.findViewById<View>(R.id.back)!!
                .setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

            mView?.findViewById<View>(R.id.logout)!!.setOnClickListener {
                val dialog = Dialog(requireActivity())
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
                content.text = "是否確認登出？"
                val noBtn: TextView = dialog.findViewById(R.id.no)
                noBtn.text = "取消"
                noBtn.setOnClickListener {
                    dialog.dismiss()
                }
                val yesBtn: TextView = dialog.findViewById(R.id.yes)
                yesBtn.text = "確認"
                yesBtn.setOnClickListener {
                    NewTaipeiSDKApi.getInstance().logout(
                        requireActivity(),
                        UserInfoManager.getInstance().getUserInfo(requireActivity())!!.account,
                        object : NetworkManager.NetworkManagerListener<LogoutResponse> {
                            override fun onSucceed(logoutResponse: LogoutResponse) {
                                PreferencesTools.getInstance().saveProperty(
                                    requireActivity(),
                                    PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD, "false"
                                )
                                PreferencesTools.getInstance().saveProperty(
                                    requireActivity(),
                                    PreferencesTools.KEY_PREFERENCES_BIOMETRIC, "false"
                                )
                                UserInfoManager.getInstance().userLogout(requireActivity())

                                dialog.dismiss()

                                val intent =
                                    Intent(requireActivity(), LoginActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }

                            override fun onFail(errorMsg: String, shouldRetry: Boolean) {
                                var error = errorMsg
                                if (errorMsg.contains("ACCESS_DENY"))
                                    error = "安全驗證失敗"
                                else if (errorMsg.contains("INVALID_PARAMETER"))
                                    error = "傳入參數錯誤"
                                DialogTools.getInstance().showErrorMessage(
                                    requireActivity(),
                                    R.string.dialog_title_text_note,
                                    error
                                )
                            }
                        })
                }
                dialog.show()
            }

            mView?.findViewById<View>(R.id.info)!!.setOnClickListener {
                val packageManager = requireActivity().packageManager
                val packageInfo: PackageInfo
                var versionName = ""
                try {
                    packageInfo = packageManager.getPackageInfo(requireActivity().packageName, 0)
                    versionName = packageInfo.versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                val androidVersion = Build.VERSION.RELEASE
                val sdkVersion = Build.VERSION.SDK_INT

                val dialog = Dialog(requireActivity())
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
                title.text = "關於"
                val content: TextView = dialog.findViewById(R.id.content)
                content.text = "程式版本：$versionName\n系統版本：Android $androidVersion"
                val btn: TextView = dialog.findViewById(R.id.btn)
                btn.text = "確認"
                btn.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }

            switchPassword = mView?.findViewById(R.id.switchPassword)
            val keepPassword = if (PreferencesTools.getInstance().getProperty(
                    requireActivity(),
                    PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD
                ) != null
            ) {
                PreferencesTools.getInstance().getProperty(
                    requireActivity(),
                    PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD
                )
            } else {
                "false"
            }
            switchPassword?.setChecked(keepPassword == "true")
            switchPassword?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    PreferencesTools.getInstance().saveProperty(
                        requireActivity(),
                        PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD, "true"
                    )
                } else {
                    PreferencesTools.getInstance().saveProperty(
                        requireActivity(),
                        PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD, "false"
                    )
                }
            })

            switchFingerprint = mView?.findViewById(R.id.switchFingerprint)
            val biometric = if (PreferencesTools.getInstance().getProperty(
                    requireActivity(),
                    PreferencesTools.KEY_PREFERENCES_BIOMETRIC
                ) != null
            ) {
                PreferencesTools.getInstance().getProperty(
                    requireActivity(),
                    PreferencesTools.KEY_PREFERENCES_BIOMETRIC
                )
            } else {
                "false"
            }
            switchFingerprint?.setChecked(biometric == "true")
            switchFingerprint?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    val dialog = Dialog(requireActivity())
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.dialog_password)
                    dialog.setCancelable(false)
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val width =
                        (resources.displayMetrics.widthPixels * 0.9).toInt()
                    dialog.window!!.setLayout(
                        width,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val account: TextView = dialog.findViewById(R.id.account)
                    account.text = "帳號：" + userid
                    val passwordEt: EditText = dialog.findViewById(R.id.password_et)
                    val noBtn: TextView = dialog.findViewById(R.id.no)
                    noBtn.setOnClickListener {
                        dialog.dismiss()
                        switchFingerprint?.isChecked = false
                    }
                    val yesBtn: TextView = dialog.findViewById(R.id.yes)
                    yesBtn.setOnClickListener {
                        dialog.dismiss()
                        val pwd =
                            UserInfoManager.getInstance().getUserInfo(requireActivity())!!.password
                        if (passwordEt.text.trim().toString() == pwd) {
                            PreferencesTools.getInstance().saveProperty(
                                requireActivity(),
                                PreferencesTools.KEY_PREFERENCES_BIOMETRIC, "true"
                            )
                        } else {
                            switchFingerprint?.isChecked = false
                            DialogTools.getInstance().showErrorMessage(
                                requireActivity(), R.string.dialog_title_text_note,
                                R.string.activity_login_pwd_error
                            )
                        }
                    }
                    dialog.show()
                } else {
                    PreferencesTools.getInstance().saveProperty(
                        requireActivity(),
                        PreferencesTools.KEY_PREFERENCES_BIOMETRIC, "false"
                    )
                }
            })
        }
        return mView
    }

    companion object {
        const val TAG: String = "fragment_tag_menu"

        fun newInstance(): MenuFragment {
            return MenuFragment()
        }
    }
}
