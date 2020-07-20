#include <jni.h>
#include <string>
#include <android/Log.h>
#define LOG_TAG "linb"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

//The purpose of this c++ code is to save the import string in c++ to prevent reverse of java string

static jstring
getAESKey(JNIEnv *env, jobject thiz) {
    LOGI("Call getAESKey!\n");
    //change you string aeskey for beacon here
    std::string aesKey = "0227022850022709690808932355050f";
    return env->NewStringUTF(aesKey.c_str());
}
static jstring
getIVector(JNIEnv *env, jobject thiz) {
    LOGI("Call getIVector!\n");
    //change you string invector for beacon here
    std::string ivector = "65cc4c0b6cf9c56e2a2d801df1b99d01"; //thie last 4 char wil be igore since we will randomize the last two byte 256x256
    return env->NewStringUTF(ivector.c_str());
}

//Class path name for Register
static const char *classPathName = "omni/com/newtaipeisdk/beacon/BaseBleActivity"; // attached the code to first activity, if you do not have MainActivity, you have to change it
//定义方法隐射关系
// java方法名称, java方法签名 ,c/c++的函数指针
static JNINativeMethod methods[] = {

        {"getAESKey", "()Ljava/lang/String;", (void*)getAESKey},
        {"getIVector", "()Ljava/lang/String;", (void*)getIVector},

};

/*
* Register several native methods for one class.
*/
static int registerNativeMethods(JNIEnv* env, const char* className,
                                 JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGI("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGI("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
    if (!registerNativeMethods(env, classPathName,
                               methods, sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }


    return JNI_TRUE;
}
// ----------------------------------------------------------------------------
/*
 * This is called by the VM when the shared library is first loaded.
 */

typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;

    LOGI("JNI_OnLoad");
    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        LOGI("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;
    if (registerNatives(env) != JNI_TRUE) {
        LOGI("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

    bail:
    return result;
}
void onInit(JNIEnv* env){
    jclass clz = env->FindClass(classPathName);
    if (env->ExceptionOccurred()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        return;
    }
    __android_log_print(ANDROID_LOG_ERROR, "Load Native Lib", "Error find lib");
}
/////////////////////////////////////////////////////////////////////////


//This is creade by ide android studio, we will ignore it
//original method
//you will need this declaration
/*
extern "C" {
JNIEXPORT jstring JNICALL Java_com_m4grid_baseline_MainActivity_stringFromJNI(JNIEnv* env,
                                                                              jobject thiz );
}
JNIEXPORT jstring JNICALL
Java_com_m4grid_baseline_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject thiz) {
    LOGI("Call stringFromJNI!\n");
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
*/

