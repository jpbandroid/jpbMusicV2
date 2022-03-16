package com.jpb.eleven.about

import android.annotation.SuppressLint
import org.lineageos.eleven.R
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import com.drakeet.about.*
import org.lineageos.eleven.BuildConfig


class AboutActivity : AbsAboutActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreateHeader(
        @NonNull icon: ImageView,
        @NonNull slogan: TextView,
        @NonNull version: TextView
    ) {
        icon.setImageResource(R.mipmap.ic_launcher)
        slogan.text = "jpb Music v2 (Eleven)"
        version.text = BuildConfig.VERSION_NAME
    }

    override fun onItemsCreated(@NonNull items: MutableList<Any>) {
        items.add(Category("About app"))
        items.add(Card("jpb Music\nFOSS music player for Android based on LineageOS' Eleven music player"))
        items.add(Category("Developers"))
        items.add(
            Contributor(
                R.drawable.jpb,
                "jpb",
                "Developer",
                "https://occoam.com/jpb"
            )
        )
        items.add(Contributor(R.drawable.lineage, "The LineageOS Project", "Original app's developers", "https://lineageos.org/"))
    }
}