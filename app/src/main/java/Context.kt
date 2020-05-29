import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.openLink(url: String?) {
    val link = url ?: return
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    if (browserIntent.resolveActivity(packageManager) != null)
        startActivity(browserIntent)
    else Toast.makeText(this, "Cannot find a browser", Toast.LENGTH_LONG).show()
}