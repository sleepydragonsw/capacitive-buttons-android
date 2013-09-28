#include <jni.h>
#include <string.h>
#include <errno.h>

#include "CLib_helper.h"

void CLib_ThrowClibException(JNIEnv *jenv) {
    int saved_errno = errno;

    jclass cls = (*jenv)->FindClass(jenv, "org/sleepydragon/capbutnbrightness/clib/ClibException");
    if (cls == NULL) {
        return;
    }

    jmethodID constructorId = (*jenv)->GetMethodID(jenv, cls, "<init>", "(Ljava/lang/String;I)V");
    if (constructorId == NULL) {
        return;
    }

    const char *message = strerror(errno);
    jstring messageStr = (*jenv)->NewStringUTF(jenv, message);
    if (messageStr == NULL) {
        return;
    }

    jobject exception = (*jenv)->NewObject(jenv, cls, constructorId, messageStr, saved_errno);
    if (exception == NULL) {
        return;
    }

    (*jenv)->Throw(jenv, exception);
}
