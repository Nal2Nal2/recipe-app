package com.nal2nal2.recipe;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/** '+ 캡처 추가'를 누르면 갤러리 앱을 열어 사진을 고르게 해요. 고른 사진은 앱 임시 폴더에 복사해서 위치를 돌려줘요. */
@CapacitorPlugin(name = "GalleryPicker")
public class GalleryPickerPlugin extends Plugin {
    private static final String[] GALLERIES = {"com.sec.android.gallery3d", "com.google.android.apps.photos"};

    @PluginMethod
    public void pick(PluginCall call) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        PackageManager pm = getContext().getPackageManager();
        for (String pkg : GALLERIES) {
            Intent t = new Intent(i).setPackage(pkg);
            List<ResolveInfo> r = pm.queryIntentActivities(t, 0);
            if (r != null && !r.isEmpty()) { i = t; break; }
        }
        try {
            startActivityForResult(call, i, "picked");
        } catch (Exception e) {
            call.reject("갤러리를 열지 못했어요", "NO_GALLERY");
        }
    }

    @ActivityCallback
    private void picked(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            JSObject ret = new JSObject();
            ret.put("files", new JSArray());
            call.resolve(ret);
            return;
        }
        Intent data = result.getData();
        List<Uri> uris = new ArrayList<>();
        ClipData clip = data.getClipData();
        if (clip != null) for (int k = 0; k < clip.getItemCount(); k++) uris.add(clip.getItemAt(k).getUri());
        else if (data.getData() != null) uris.add(data.getData());

        File dir = new File(getContext().getCacheDir(), "picked");
        dir.mkdirs();
        JSArray files = new JSArray();
        long stamp = System.currentTimeMillis();
        for (int k = 0; k < uris.size(); k++) {
            File out = new File(dir, "p" + stamp + "_" + k + ".jpg");
            try (InputStream in = getContext().getContentResolver().openInputStream(uris.get(k));
                 OutputStream os = new FileOutputStream(out)) {
                if (in == null) continue;
                byte[] buf = new byte[65536];
                int n;
                while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
                JSObject o = new JSObject();
                o.put("path", out.getAbsolutePath());
                files.put(o);
            } catch (Exception ignored) { }
        }
        JSObject ret = new JSObject();
        ret.put("files", files);
        call.resolve(ret);
    }
}
