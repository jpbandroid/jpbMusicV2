package com.jpb.eleven.utils

import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

class inflateBinding(layoutInflater: LayoutInflater) {

    @Suppress("UNCHECKED_CAST")
    fun <T : ViewBinding> LifecycleOwner.inflateBinding(inflater: LayoutInflater): T {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
            .filterIsInstance<Class<T>>()
            .first()
            .getDeclaredMethod("inflate", LayoutInflater::class.java)
            .invoke(null, inflater) as T
    }
}