package com.jpb.eleven.about

import android.annotation.SuppressLint
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import org.lineageos.eleven.R
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.drakeet.about.*
import com.jpb.eleven.oss.licenses.OSSLicense
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
        window.statusBarColor = ContextCompat.getColor(getApplicationContext(), R.color.accent);
        setHeaderBackground(R.color.accent)
        setHeaderContentScrim(R.color.accent)
    }

    override fun onItemsCreated(@NonNull items: MutableList<Any>) {
        items.add(Category("About app"))
        items.add(Card("jpb Music\nFOSS music player for Android based on LineageOS' Eleven music player\nEleven source: https://github.com/LineageOS/android_packages_apps_Eleven/tree/lineage-20.0\njpb Music source: https://github.com/jpbandroid/jpbMusicV2"))
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_about, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.osslicense) {
            val intent = Intent(applicationContext, OSSLicense::class.java)
            startActivity(intent)
            return true
        }
        return false
    }
}