package com.yong.munittest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;
//import junit.framework.Assert;
//import junit.framework.Test;
import org.junit.Test;
//import org.junit.Assert.*;
import org.junit.Assert;
import org.junit.runner.RunWith;

import java.io.FileOutputStream;

/**
 * Created by yong on 10/8/15.
 */
//@RunWith(AndroidJUnit4.class)
public class EmailValTest extends InstrumentationTestCase{
    private Resources mRes;
    @Override
    protected void setUp() throws Exception{
        mRes = getInstrumentation().getTargetContext().getResources();
    }
    @Test
    public void test_emailValidator_CorrectEmSimple_RetTrue() throws Exception {
        Bitmap bmp;
        FileOutputStream fos;
        fos = getInstrumentation().getTargetContext().openFileOutput("sss.webp", Context.MODE_MULTI_PROCESS);
        //fos = new FileOutputStream("sss.webp");
        //mRes.openRawResource(R.drawable.sss);
        //openFileOu
        bmp = BitmapFactory.decodeResource(mRes,R.drawable.sss);
        //bmp = BitmapFactory.decodeFile("/data/data/sss.jpg");

        Assert.assertNotNull("BMP is null",bmp);
        Assert.assertTrue("compress failed", bmp.compress(Bitmap.CompressFormat.WEBP, 100, fos));
    }
}
