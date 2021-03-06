package com.example.gfc.gaidelclicker.skin;

import com.example.gfc.gaidelclicker.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class SkinManager {

    private static List<Skin> skins;

    static {
        skins = new ArrayList<>();
        skins.add(new Skin(R.drawable.gaidel_face_gold_ny, R.drawable.svas_ny, R.drawable.background_ny, 11, 15, 11, 31));//NY
        skins.add(new Skin(R.drawable.gaidel_face_gold_ny, R.drawable.svas_ny, R.drawable.background_ny, 0, 0, 0, 14));//NY
        skins.add(new Skin(R.drawable.gaidel_face_vdve, R.drawable.svas2, R.drawable.background_camo, 1, 22, 1, 25));//22-25 feb
        skins.add(new Skin(R.drawable.gaidel_dark_with_rose, R.drawable.svas_flower, R.drawable.background_roses, 2, 7, 2, 10));//7-10 mar
    }

    public static Skin findActiveSkin() {
        GregorianCalendar calendar = new GregorianCalendar();
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        for (Skin skin : skins) {
            if (skin.availableNow(day, month)) return skin;
        }
        return null;
    }
}
