package omni.com.newtaipeisdk.tool;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import omni.com.newtaipeisdk.R;

public class Tools {

    private static Tools mTools;

    public static int STATUS_BAR = 0;
    private static final float beaconTrigger10 = 10f;

    public float getBeaconTrigger() {
        return beaconTrigger10;
    }

    public static Tools getInstance() {
        if (mTools == null) {
            mTools = new Tools();
        }
        return mTools;
    }

    private int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }

    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void hideKeyboard(Context context, View rootView) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    public int getTabBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        } else {
            return dpToIntPx(context, 55);
        }
    }

    public int dpToIntPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public Drawable getDrawable(Context context, int drawableId) {
        return ContextCompat.getDrawable(context, drawableId);
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    static byte[] iv = new byte[]{111, 103, 97, 109, 105, 110, 58, 47, 47, 110, 108, 112, 105, 97, 112, 112};
    static byte[] key = new byte[]{110, 108, 112, 105, 97, 112, 112, 58, 47, 47, 111, 103, 97, 109, 105, 110, 110, 108, 112, 105, 97, 112, 112, 58, 47, 47, 111, 103, 97, 109, 105, 110};

    public static byte[] EncryptAES(byte[] text) {
        try {
            AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
            Cipher mCipher = null;
            mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec);

            return mCipher.doFinal(text);
        } catch (Exception ex) {
            return null;
        }
    }

    public static byte[] DecryptAES(byte[] text) {
        try {
            AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
            Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            mCipher.init(Cipher.DECRYPT_MODE,
                    mSecretKeySpec,
                    mAlgorithmParameterSpec);

            return mCipher.doFinal(text);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String encrypt(String encrypt) {
        String encrypt_String = "";
        try {
            if (encrypt != null) {
                byte[] TextByte = EncryptAES(encrypt.getBytes("UTF-8"));
                encrypt_String = Base64.encodeToString(TextByte, Base64.DEFAULT);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encrypt_String;
    }

    public static String decrypt(String decrypt) {
        String decrypt_String = "";
        try {
            if (decrypt != null) {
                byte[] TextByte = DecryptAES(Base64.decode(decrypt.getBytes("UTF-8"), Base64.DEFAULT));
                if (TextByte != null) {
                    decrypt_String = new String(TextByte, "UTF-8");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decrypt_String;
    }

}
