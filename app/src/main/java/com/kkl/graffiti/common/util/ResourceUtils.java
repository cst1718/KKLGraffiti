package com.kkl.graffiti.common.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import java.io.IOException;

public class ResourceUtils {

    public static Context sAppContext;

    public static void init(Context ctx) {
        sAppContext = ctx.getApplicationContext();
    }

    /**
     * 取指定资源颜色对像值
     *
     * @param ColorId
     * @return
     */
    public static int getResourcesColor(int ColorId) {
        return getResourcesColor(ColorId, 0xFFFFFFFF);
    }

    /**
     * 取指定资源颜色对像值
     *
     * @param ColorId 颜色ID
     * @return
     */
    public static int getResourcesColor(int ColorId, int DefaultColor) {
        if (ColorId == -1)
            return DefaultColor;
        if (sAppContext != null) {
            try {
                return sAppContext.getResources().getColor(ColorId);
            } catch (Resources.NotFoundException mx) {
            }
        }
        return DefaultColor;
    }

    /**
     * 取指定资源字符串对像值
     *
     * @param StringID
     * @return
     */
    public static String getResourcesString(int StringID) {
        if (StringID == -1)
            return "";
        if (sAppContext != null) {
            try {
                return sAppContext.getResources().getString(StringID);
            } catch (Resources.NotFoundException mx) {
            }
        }
        return "";
    }

    /**
     * 取指定资源字符串对像值(例如 Hi,%1$s你好吗?)
     *
     * @param StringID
     * @param obj      数据填充列表
     * @return
     */
    public static String getResourcesString(int StringID, Object... obj) {
        return String.format(getResourcesString(StringID), obj);
    }

    /**
     * 取指定资源尺寸对象值
     *
     * @param StringID
     * @return
     */
    public static float getResourcesDimension(int StringID) {
        if (StringID == -1)
            return 0;
        if (sAppContext != null) {
            try {
                return sAppContext.getResources().getDimension(StringID);
            } catch (Resources.NotFoundException mx) {
            }
        }
        return 0;
    }

    public static int getResourcesDimensionforInt(int StringID) {
        if (StringID == -1)
            return 0;
        if (sAppContext != null) {
            try {
                return (int) (sAppContext.getResources().getDimension(StringID) + 0.5f);
            } catch (Resources.NotFoundException mx) {
            }
        }
        return 0;
    }

    /**
     * 取指定资源尺寸对象值
     *
     * @param drawableId
     * @return
     */
    public static Drawable getResourcesDrawable(int drawableId) {
        if (drawableId == -1)
            return null;
        if (sAppContext != null) {
            try {
                return sAppContext.getResources().getDrawable(drawableId);
            } catch (Resources.NotFoundException mx) {
            }
        }
        return null;
    }

    /**
     * 取指定资源字符串数组对象
     *
     * @param StringID
     * @return
     */
    public static String[] getResourcesStringArray(int StringID) {
        if (StringID == -1)
            return null;
        if (sAppContext != null) {
            try {
                return sAppContext.getResources().getStringArray(StringID);
            } catch (Resources.NotFoundException mx) {
            }
        }
        return null;
    }

    /**
     * 获取资源
     * @param context
     * @param attrId
     * @return
     */
    public static int getResourceId(Context context, int attrId){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{attrId});
        int resourceId = typedArray.getResourceId(typedArray.getIndex(0),0);
        typedArray.recycle();
        return resourceId;
    }

    public static String getAssertContent(Context ctx, String fName) {
        if (null == ctx || null == fName) return null;
        AssetManager manager = ctx.getApplicationContext().getAssets();

        try {
            return FileUtils.bufferRead(manager.open(fName), "UTF-8");
        } catch (IOException e) {
            return null;
        }
    }
}
